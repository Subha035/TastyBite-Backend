package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String id;
    private String sender; // "user" or "bot"
    private String text;
    private String time;
    private List<MenuItemDto> pizzaList;
    private String subtext;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemDto {
        private String title;
        private String desc;
        private String price;
        private String image;
        private Boolean isVeg;
    }
}
