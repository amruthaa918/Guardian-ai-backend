package com.guardianai.service;

import com.guardianai.dto.AIRequest;
import com.guardianai.dto.AIResponse;
import com.guardianai.entity.AIAnalysis;
import com.guardianai.entity.User;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.repository.AIAnalysisRepository;
import com.guardianai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class AIService {

    private final AIAnalysisRepository aiAnalysisRepository;
    private final UserRepository userRepository;

    // High risk triggers
    private static final List<String> HIGH_RISK_KEYWORDS = Arrays.asList(
            "attack", "injured", "injury", "trapped", "kidnapping", "kidnap",
            "threatening", "threat", "being followed", "followed",
            "immediate danger", "danger", "emergency", "help me", "weapon",
            "robbery", "stalker", "harassment"
    );

    // Medium risk triggers
    private static final List<String> MEDIUM_RISK_KEYWORDS = Arrays.asList(
            "alone at night", "uncomfortable", "suspicious", "unsafe",
            "worried", "stranger", "travelling alone", "traveling alone",
            "dark alley", "lost", "cab", "taxi", "breakdown"
    );

    public AIService(AIAnalysisRepository aiAnalysisRepository, UserRepository userRepository) {
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AIResponse analyzeSituation(Long userId, AIRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String situation = request.getSituation().trim();
        AIResponse assessment = evaluateRisk(situation);

        AIAnalysis analysis = new AIAnalysis();
        analysis.setUser(user);
        analysis.setSituation(situation);
        analysis.setRiskLevel(assessment.getRiskLevel());
        analysis.setRecommendation(assessment.getRecommendation());

        aiAnalysisRepository.save(analysis);

        return assessment;
    }

    /**
     * Isolated rule-based evaluation logic.
     * Can easily be replaced or augmented with an external LLM/AI model in the future.
     */
    public AIResponse evaluateRisk(String situation) {
        if (situation == null || situation.trim().isEmpty()) {
            return new AIResponse("LOW", "No situation specified. Stay alert and aware of your surroundings.");
        }

        String lowerCaseSituation = situation.toLowerCase();

        // 1. Check for High Risk indicators
        for (String keyword : HIGH_RISK_KEYWORDS) {
            if (lowerCaseSituation.contains(keyword)) {
                return new AIResponse(
                        "HIGH",
                        "Immediate danger detected! Trigger Emergency SOS now, call local emergency services (112/100/911), and seek shelter in a well-lit crowded area."
                );
            }
        }

        // 2. Check for Medium Risk indicators
        for (String keyword : MEDIUM_RISK_KEYWORDS) {
            if (lowerCaseSituation.contains(keyword)) {
                return new AIResponse(
                        "MEDIUM",
                        "Share your location with a trusted contact and enable Safety Check-In."
                );
            }
        }

        // 3. Default to Low Risk
        return new AIResponse(
                "LOW",
                "Situation appears normal and safe. Stay aware of your surroundings, ensure your phone is charged, and keep emergency contacts accessible."
        );
    }
}
