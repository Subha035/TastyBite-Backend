package com.example.backend.service;

import com.example.backend.model.ChatMessage;
import com.example.backend.model.MenuItem;
import com.example.backend.model.Offer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private LlmService llmService;

    public ChatMessage processUserMessage(String userText) {
        String lowerText = userText.trim().toLowerCase();
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));

        List<MenuItem> allMenuItems = firebaseService.getAllMenuItems();
        List<Offer> allOffers = firebaseService.getAllOffers();

        // Generate response using LLM (Hugging Face / GenAI / LangChain RAG)
        String llmText = llmService.generateLlmResponse(userText, allMenuItems, allOffers);

        ChatMessage response = new ChatMessage();
        response.setId(String.valueOf(System.currentTimeMillis()));
        response.setSender("bot");
        response.setTime(currentTime);
        response.setText(llmText);

        // Attach interactive menu card DTOs only if matching items exist in the database
        if (allMenuItems != null && !allMenuItems.isEmpty()) {
            boolean askingNonVeg = lowerText.contains("non veg") || lowerText.contains("non-veg") || lowerText.contains("nonveg") || lowerText.contains("chicken") || lowerText.contains("meat") || lowerText.contains("mutton") || lowerText.contains("egg") || lowerText.contains("fish");
            boolean askingVeg = !askingNonVeg && (lowerText.contains("veg") || lowerText.contains("vegetarian"));

            List<MenuItem> matchingItems = allMenuItems.stream()
                    .filter(item -> {
                        boolean isVeg = LlmService.isVegItem(item);
                        if (askingNonVeg) return !isVeg;
                        if (askingVeg) return isVeg;
                        String cat = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
                        String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
                        if (lowerText.contains("pizza")) return cat.contains("pizza") || title.contains("pizza");
                        if (lowerText.contains("burger")) return cat.contains("burger") || title.contains("burger");
                        if (lowerText.contains("coffee") || lowerText.contains("drink")) return cat.contains("coffee") || cat.contains("drink") || title.contains("coffee") || title.contains("cappuccino");
                        if (lowerText.contains("dessert") || lowerText.contains("sweet")) return cat.contains("dessert") || title.contains("cake") || title.contains("brownie");
                        return title.toLowerCase().contains(lowerText) || cat.toLowerCase().contains(lowerText);
                    })
                    .limit(3)
                    .collect(Collectors.toList());

            if (!matchingItems.isEmpty()) {
                List<ChatMessage.MenuItemDto> dtos = new ArrayList<>();
                for (MenuItem item : matchingItems) {
                    boolean isVegState = LlmService.isVegItem(item);
                    dtos.add(new ChatMessage.MenuItemDto(
                            item.getTitle(),
                            item.getDesc(),
                            "₹" + (item.getPrice() != null ? item.getPrice().intValue() : 0),
                            item.getImage() != null ? item.getImage() : "/margherita_pizza.png",
                            isVegState
                    ));
                }
                response.setPizzaList(dtos);
                response.setSubtext("Would you like to order any of these dishes?");
            } else if (lowerText.contains("offer") || lowerText.contains("discount") || lowerText.contains("deal")) {
                response.setSubtext("Which of these coupon codes would you like to use?");
            } else {
                response.setSubtext("How else can I assist you with TastyBite?");
            }
        } else {
            response.setSubtext("How else can I assist you with TastyBite?");
        }

        return response;
    }
}

