package com.example.backend.service;

import com.example.backend.config.JwtUtils;
import com.example.backend.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public Map<String, Object> signup(String name, String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        User existing = firebaseService.getUserByEmail(email);
        if (existing != null) {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        User user = new User();
        user.setName(name != null && !name.trim().isEmpty() ? name.trim() : email.split("@")[0]);
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setAuthProvider("local");
        user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + email);

        User savedUser = firebaseService.saveUser(user);
        String token = jwtUtils.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getName(), savedUser.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", sanitizeUser(savedUser));
        return response;
    }

    public Map<String, Object> login(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        User user = firebaseService.getUserByEmail(email);
        if (user == null || user.getPasswordHash() == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getName(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", sanitizeUser(user));
        return response;
    }

    public Map<String, Object> loginWithGoogle(String idToken, String email, String name, String picture) {
        String googleEmail = email;
        String googleName = name;
        String googlePicture = picture;

        // Verify Firebase / Google ID token if provided and FirebaseApp is initialized
        if (idToken != null && !idToken.trim().isEmpty()) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                googleEmail = decodedToken.getEmail();
                googleName = decodedToken.getName();
                googlePicture = decodedToken.getPicture();
            } catch (Exception e) {
                System.err.println("Firebase ID Token verification note: " + e.getMessage() + ". Fallback to payload claims.");
            }
        }

        if (googleEmail == null || googleEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Google authentication failed. No valid email found.");
        }

        User user = firebaseService.getUserByEmail(googleEmail);
        if (user == null) {
            user = new User();
            user.setName(googleName != null ? googleName : googleEmail.split("@")[0]);
            user.setEmail(googleEmail.toLowerCase());
            user.setAvatar(googlePicture != null ? googlePicture : "https://api.dicebear.com/7.x/avataaars/svg?seed=" + googleEmail);
            user.setRole("USER");
            user.setAuthProvider("google");
            user = firebaseService.saveUser(user);
        } else if (googlePicture != null) {
            user.setAvatar(googlePicture);
            firebaseService.saveUser(user);
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getName(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", sanitizeUser(user));
        return response;
    }

    public Map<String, Object> getCurrentUser(String token) {
        if (token == null || !jwtUtils.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token.");
        }
        String userId = jwtUtils.getUserIdFromToken(token);
        User user = firebaseService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("user", sanitizeUser(user));
        return response;
    }

    private Map<String, Object> sanitizeUser(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("name", user.getName());
        dto.put("email", user.getEmail());
        dto.put("avatar", user.getAvatar());
        dto.put("role", user.getRole());
        dto.put("authProvider", user.getAuthProvider());
        dto.put("createdAt", user.getCreatedAt());
        return dto;
    }
}
