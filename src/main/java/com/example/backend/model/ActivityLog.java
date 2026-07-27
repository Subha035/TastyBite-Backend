package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    private String id;
    private String title;
    private String category; // "menu", "stock", "coupon", "reservation"
    private String time;     // e.g. "Just now", "2m ago"
    private Long timestamp;
}
