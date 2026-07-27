package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String date;
    private String time;
    private Integer guests;
    private String tableType;
    private String specialRequest;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private String createdAt;
}
