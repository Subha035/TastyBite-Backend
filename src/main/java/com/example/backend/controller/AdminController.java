package com.example.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final String ADMIN_EMAIL = "tastyadmin@tastybite.com";
    private static final String ADMIN_PASSWORD = "TastyAdmin@035";

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (ADMIN_EMAIL.equalsIgnoreCase(email) && ADMIN_PASSWORD.equals(password)) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", "mock-jwt-token-tastybite-admin-2026",
                    "adminEmail", ADMIN_EMAIL,
                    "message", "Login successful"
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Invalid admin email or password"
        ));
    }
}
