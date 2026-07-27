package com.example.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore firestore() {
        try {
            // Prevent multiple initializations
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirestoreClient.getFirestore();
            }

            InputStream serviceAccount;

            // Read Firebase JSON from Render Environment Variable
            String firebaseJson = System.getenv("FIREBASE_SERVICE_ACCOUNT");

            if (firebaseJson != null && !firebaseJson.isBlank()) {
                serviceAccount = new ByteArrayInputStream(
                        firebaseJson.getBytes(StandardCharsets.UTF_8));
            } else {
                // Local development (uses src/main/resources/firebase-service-account.json)
                serviceAccount = getClass()
                        .getClassLoader()
                        .getResourceAsStream("firebase-service-account.json");
            }

            if (serviceAccount == null) {
                throw new RuntimeException("Firebase credentials not found.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

            return FirestoreClient.getFirestore();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}
