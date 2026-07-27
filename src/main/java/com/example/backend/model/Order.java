package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String id;
    private String orderId;
    private List<OrderItem> items;
    private String tableNumber;
    private Double totalAmount;
    private String status; // PENDING, PREPARING, SERVED, COMPLETED, CANCELLED
    private String customerName;
    private String customerPhone;
    private String paymentStatus;
    private String qrCodeUrl;
    private String createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String id;
        private String title;
        private Double price;
        private Integer quantity;
        private String specialNotes;
    }
}
