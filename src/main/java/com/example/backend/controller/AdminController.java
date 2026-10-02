package com.example.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(originPatterns = "*")
public class AdminController {

    private static final String ADMIN_USERNAME = "Admin035";
    private static final String ADMIN_PASSWORD = "Admin@035";
    private static final String ADMIN_EMAIL = "tastyadmin@tastybite.com";

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String email = credentials.get("email");
        String password = credentials.get("password");

        boolean isValidUser = (username != null && ADMIN_USERNAME.equalsIgnoreCase(username.trim()))
                || (email != null && (ADMIN_EMAIL.equalsIgnoreCase(email.trim()) || ADMIN_USERNAME.equalsIgnoreCase(email.trim())));
        boolean isValidPass = ADMIN_PASSWORD.equals(password);

        if (isValidUser && isValidPass) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", "mock-jwt-token-tastybite-admin-2026",
                    "adminUsername", ADMIN_USERNAME,
                    "message", "Login successful"
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Invalid admin username or password"
        ));
    }
}
