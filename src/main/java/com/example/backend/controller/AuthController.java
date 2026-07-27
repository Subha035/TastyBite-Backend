package com.example.backend.controller;

import com.example.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    // Signup
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> request) {

        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");

            Map<String, Object> response =
                    authService.signup(name, email, password);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Registration failed"
                    ));
        }
    }


    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        try {
            String email = request.get("email");
            String password = request.get("password");


            Map<String, Object> response =
                    authService.login(email, password);


            return ResponseEntity.ok(response);


        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", e.getMessage()
                    ));


        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Login failed"
                    ));
        }
    }



    // Google Login
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(
            @RequestBody Map<String, String> request) {

        try {

            String idToken = request.get("idToken");
            String email = request.get("email");
            String name = request.get("name");
            String picture = request.get("picture");


            Map<String, Object> response =
                    authService.loginWithGoogle(
                            idToken,
                            email,
                            name,
                            picture
                    );


            return ResponseEntity.ok(response);


        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message", e.getMessage()
                    ));


        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Google login failed"
                    ));
        }
    }



    // Current user profile
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {


        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "message", "Missing token"
                        ));
            }


            String token = authHeader.substring(7);


            Map<String, Object> response =
                    authService.getCurrentUser(token);


            return ResponseEntity.ok(response);



        } catch (IllegalArgumentException e) {


            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", e.getMessage()
                    ));


        } catch (Exception e) {


            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Unable to fetch user"
                    ));
        }
    }
}
