package com.guardianai.controller;

import com.guardianai.dto.AIRequest;
import com.guardianai.dto.AIResponse;
import com.guardianai.security.UserPrincipal;
import com.guardianai.service.AIService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AIResponse> analyzeSituation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AIRequest request
    ) {
        AIResponse response = aiService.analyzeSituation(userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }
}
