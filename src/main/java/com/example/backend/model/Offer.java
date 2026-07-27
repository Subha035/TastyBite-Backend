package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offer {
    private String id;
    private String title;
    private String code;
    private Double discountPercent;
    private Double maxDiscount;
    private Double minOrder;
    private String desc;
    private String validTill;
    private Boolean active;
}
