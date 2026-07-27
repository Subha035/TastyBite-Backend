package com.example.backend.controller;

import com.example.backend.model.ActivityLog;
import com.example.backend.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {

    @Autowired
    private FirebaseService firebaseService;

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getActivities() {
        List<ActivityLog> logs = firebaseService.getAllActivities();
        return ResponseEntity.ok(logs);
    }

    @PostMapping
    public ResponseEntity<ActivityLog> logActivity(@RequestBody ActivityLog request) {
        if (request == null || request.getTitle() == null) {
            return ResponseEntity.badRequest().build();
        }
        ActivityLog saved = firebaseService.logActivity(request.getTitle(), request.getCategory());
        return ResponseEntity.ok(saved);
    }
}
