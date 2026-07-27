package com.example.backend.service;

import com.example.backend.model.MenuItem;
import com.example.backend.model.Offer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class LlmService {


    @Value("${genai.api.key:}")
    private String rawGenaiApiKey;

    @Value("${genai.model:gemini-2.5-flash}")
    private String genaiModel;

    private final RestTemplate restTemplate = new RestTemplate();

    private String cleanKey(String rawKey) {
        if (rawKey == null) return "";
        return rawKey.replace("\"", "").replace("'", "").trim();
    }

    /**
     * LangChain-style RAG Prompt Builder combining live Database context + User query
     */
    public String buildSystemPrompt(String userMessage, List<MenuItem> menuItems, List<Offer> offers) {
        StringBuilder menuContext = new StringBuilder();
        if (menuItems != null && !menuItems.isEmpty()) {
            for (MenuItem item : menuItems) {
                menuContext.append("- ").append(item.getTitle())
                        .append(" (Category: ").append(item.getCategory() != null ? item.getCategory() : "General")
                        .append(", Price: ₹").append(item.getPrice() != null ? item.getPrice().intValue() : 0)
                        .append(", Veg: ").append(item.getIsVeg() != null && item.getIsVeg() ? "Yes" : "No")
                        .append("): ").append(item.getDesc() != null ? item.getDesc() : "")
                        .append("\n");
            }
        } else {
            menuContext.append("No active menu items currently listed in the database.\n");
        }

        StringBuilder offersContext = new StringBuilder();
        if (offers != null && !offers.isEmpty()) {
            for (Offer offer : offers) {
                offersContext.append("- ").append(offer.getTitle())
                        .append(" | Code: ").append(offer.getCode())
                        .append(" | Description: ").append(offer.getDesc() != null ? offer.getDesc() : "")
                        .append("\n");
            }
        } else {
            offersContext.append("No special discount offers currently active.\n");
        }

        return """
            System: You are TastyBite AI Assistant, a friendly and intelligent virtual host for TastyBite Restaurant.
            Answer customer questions accurately using the live restaurant database provided below.
            
            === LIVE RESTAURANT MENU ===
            %s
            === ACTIVE PROMOTIONS & OFFERS ===
            %s
            === RESTAURANT INFORMATION ===
            - Hours: Mon-Thu 11:00 AM - 11:00 PM, Fri-Sun 11:00 AM - Midnight.
            - Table Reservations: Guests can reserve tables online under the Table Reservations section.
            - Order Delivery: Orders can be placed directly from the menu section.

            User Query: "%s"

            Instructions:
            Be polite, helpful, and concise. Highlight matching menu items or offers accurately. Keep formatting clean with emojis.
            """.formatted(menuContext.toString(), offersContext.toString(), userMessage);
    }

    /**
     * Generates LLM response using Hugging Face Inference API, Google GenAI, or LangChain RAG fallback.
     */
    public String generateLlmResponse(String userMessage, List<MenuItem> menuItems, List<Offer> offers) {
        String prompt = buildSystemPrompt(userMessage, menuItems, offers);
        String genAiKey = cleanKey(rawGenaiApiKey);

        if (!genAiKey.isEmpty()) {
            try {
                String response = callGenAiApi(prompt, genAiKey);
                if (response != null && !response.isBlank()) {
                    return response;
                }
            } catch (Exception e) {
                System.err.println("Google GenAI API call warning: " + e.getMessage());
            }
        }

        return generateRagFallbackResponse(userMessage, menuItems, offers);
    }

    private String callGenAiApi(String prompt, String apiKey) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + genaiModel + ":generateContent?key=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> contents = Map.of("parts", List.of(part));
        Map<String, Object> body = Map.of("contents", List.of(contents));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map bodyMap = response.getBody();
            List candidates = (List) bodyMap.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                if (content != null) {
                    List parts = (List) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map p = (Map) parts.get(0);
                        return (String) p.get("text");
                    }
                }
            }
        }
        return null;
    }

    public static boolean isVegItem(MenuItem item) {
        if (item == null) return true;
        if (item.getIsVeg() != null) return item.getIsVeg();
        String text = ((item.getTitle() != null ? item.getTitle() : "") + " " + (item.getCategory() != null ? item.getCategory() : "")).toLowerCase();
        return !text.contains("chicken") && !text.contains("mutton") && !text.contains("meat") &&
               !text.contains("egg") && !text.contains("fish") && !text.contains("prawn") &&
               !text.contains("pepperoni") && !text.contains("bacon") && !text.contains("non-veg") && !text.contains("non veg");
    }

    private String generateRagFallbackResponse(String userMessage, List<MenuItem> menuItems, List<Offer> offers) {
        String lower = userMessage.toLowerCase().trim();
        String[] keywords = lower.split("[\\s,\\.\\?!]+");

        boolean askingNonVeg = lower.contains("non veg") || lower.contains("non-veg") || lower.contains("nonveg") || lower.contains("chicken") || lower.contains("meat") || lower.contains("mutton") || lower.contains("egg") || lower.contains("fish");
        boolean askingVeg = !askingNonVeg && (lower.contains("veg") || lower.contains("vegetarian"));
        boolean askingPizza = lower.contains("pizza");
        boolean askingBurger = lower.contains("burger");
        boolean askingCoffee = lower.contains("coffee") || lower.contains("drink") || lower.contains("beverage") || lower.contains("cappuccino");
        boolean askingDessert = lower.contains("dessert") || lower.contains("sweet");

        // 1. Search database menu items by search query matching keywords
        if (menuItems != null && !menuItems.isEmpty()) {
            List<MenuItem> matchedItems = new ArrayList<>();
            for (MenuItem item : menuItems) {
                boolean vegState = isVegItem(item);
                String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
                String category = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
                String desc = item.getDesc() != null ? item.getDesc().toLowerCase() : "";

                if (askingNonVeg) {
                    if (!vegState) matchedItems.add(item);
                } else if (askingVeg) {
                    if (vegState) matchedItems.add(item);
                } else if (askingPizza && (title.contains("pizza") || category.contains("pizza"))) {
                    matchedItems.add(item);
                } else if (askingBurger && (title.contains("burger") || category.contains("burger"))) {
                    matchedItems.add(item);
                } else if (askingCoffee && (title.contains("coffee") || title.contains("cappuccino") || category.contains("drink") || category.contains("coffee"))) {
                    matchedItems.add(item);
                } else if (askingDessert && (category.contains("dessert") || title.contains("brownie") || title.contains("cake"))) {
                    matchedItems.add(item);
                } else {
                    for (String kw : keywords) {
                        if (kw.length() >= 3 && (title.contains(kw) || category.contains(kw) || desc.contains(kw))) {
                            if (!matchedItems.contains(item)) matchedItems.add(item);
                        }
                    }
                }
            }

            if (!matchedItems.isEmpty()) {
                StringBuilder sb = new StringBuilder("Here are the matching items found in our live database:\n\n");
                for (MenuItem item : matchedItems) {
                    boolean vegState = isVegItem(item);
                    boolean inStock = item.getAvailable() == null || item.getAvailable();
                    sb.append("• **").append(item.getTitle()).append("** [")
                      .append(vegState ? "🟢 Veg" : "🔴 Non-Veg").append(" • ")
                      .append(inStock ? "In Stock" : "⚠️ Out of Stock")
                      .append("] (₹").append(item.getPrice() != null ? item.getPrice().intValue() : 0).append(") - ")
                      .append(item.getDesc() != null ? item.getDesc() : "").append("\n\n");
                }
                sb.append("Would you like to order any of these dishes?");
                return sb.toString();
            }

            if (askingNonVeg) {
                return "We currently do not have any Non-Veg items listed in our database menu yet! You can add Non-Veg items in the Admin Panel.";
            }
            if (askingVeg) {
                return "We currently do not have any Vegetarian items listed in our database menu yet! You can add Veg items in the Admin Panel.";
            }
            if (askingPizza) {
                return "We currently do not have any Pizza items in our database menu yet! You can add Pizza items in the Admin Panel.";
            }
            if (askingBurger) {
                return "We currently do not have any Burgers in our database menu yet! You can add Burger items in the Admin Panel.";
            }
        }

        // 2. Search offers
        if (lower.contains("offer") || lower.contains("discount") || lower.contains("deal") || lower.contains("coupon") || lower.contains("save")) {
            if (offers != null && !offers.isEmpty()) {
                StringBuilder sb = new StringBuilder("Here are today's active promotions from our database: 🎉\n\n");
                for (Offer offer : offers) {
                    sb.append("• **").append(offer.getTitle()).append("**\n  Use code: `")
                      .append(offer.getCode()).append("` - ")
                      .append(offer.getDesc() != null ? offer.getDesc() : "")
                      .append("\n\n");
                }
                return sb.toString();
            }
            return "No active discount coupons in the database right now. Check back soon for exciting deals!";
        }

        // 3. Opening hours / Timings
        if (lower.contains("hour") || lower.contains("time") || lower.contains("open") || lower.contains("close") || lower.contains("timing")) {
            return "TastyBite is open daily to serve you! 📍\n\n• Monday to Thursday: 11:00 AM – 11:00 PM\n• Friday to Sunday: 11:00 AM – Midnight";
        }

        // 4. Table booking / Reservations
        if (lower.contains("reserve") || lower.contains("table") || lower.contains("book") || lower.contains("seat")) {
            return "You can easily reserve a table with us! Navigate to the 'Table Reservations' tab on the left sidebar to pick your preferred date, time slot, and guest count.";
        }

        // 5. Default menu listing if user asks about menu/food/recommendations
        if (menuItems != null && !menuItems.isEmpty()) {
            StringBuilder sb = new StringBuilder("Welcome to TastyBite! Here is our current menu from the database:\n\n");
            for (MenuItem item : menuItems.stream().limit(4).toList()) {
                boolean vegState = isVegItem(item);
                boolean inStock = item.getAvailable() == null || item.getAvailable();
                sb.append("• **").append(item.getTitle()).append("** [")
                  .append(vegState ? "🟢 Veg" : "🔴 Non-Veg").append(" • ")
                  .append(inStock ? "In Stock" : "⚠️ Out of Stock")
                  .append("] (₹").append(item.getPrice() != null ? item.getPrice().intValue() : 0).append(") - ")
                  .append(item.getDesc() != null ? item.getDesc() : "").append("\n");
            }
            sb.append("\nHow can I help you with your order today?");
            return sb.toString();
        }

        return "Thank you for contacting TastyBite Assistant! I am here to help you explore our menu, special offers, table bookings, and order updates. What would you like to know?";
    }
}

