package com.example.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path:firebase-service-account.json}")
    private String configPath;

    @Value("${firebase.project.id:project-726fff49-a0ad-4aa8-990}")
    private String projectId;

@Bean
public Firestore firestore() {
    try {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirestoreClient.getFirestore();
        }

        InputStream serviceAccount;

        // First try Render Secret File
        String secretPath = System.getenv("FIREBASE_CONFIG");

        if (secretPath != null && !secretPath.isBlank()) {
            serviceAccount = new java.io.FileInputStream(secretPath);
        } else {
            // Local development
            serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");
        }

        if (serviceAccount == null) {
            throw new RuntimeException("Firebase credentials not found");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        return FirestoreClient.getFirestore();

    } catch (Exception e) {
        throw new RuntimeException("Failed to initialize Firebase", e);
    }
}
}
