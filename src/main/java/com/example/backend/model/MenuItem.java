package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    private String id;
    private String title;
    private String desc;
    private Double price;
    private String category;
    private Boolean isVeg;
    private String image;
    private Double rating;
    private Boolean available;
}
