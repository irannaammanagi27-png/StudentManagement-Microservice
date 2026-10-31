package com.example.ai_assistant_service.controller;





import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ai_assistant_service.dto.ChatRequest;
import com.example.ai_assistant_service.dto.ChatResponse;
import com.example.ai_assistant_service.service.AIService;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request) {

        ChatResponse response =
                aiService.getAIResponse(request.getMessage());

        return ResponseEntity.ok(response);
    }
}
