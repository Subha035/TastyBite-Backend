package com.example.backend.controller;

import com.example.backend.model.ChatMessage;
import com.example.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody Map<String, String> payload) {
        String messageText = payload.get("message");
        if (messageText == null || messageText.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ChatMessage botReply = chatService.processUserMessage(messageText);
        return ResponseEntity.ok(botReply);
    }
}
