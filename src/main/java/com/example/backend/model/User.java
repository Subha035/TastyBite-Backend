package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private String avatar;
    private String role; // "USER", "ADMIN"
    private String authProvider; // "local", "google"
    private String createdAt;
}
