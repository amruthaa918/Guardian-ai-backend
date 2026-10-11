package com.guardianai.service;

import com.guardianai.dto.AIRequest;
import com.guardianai.dto.AIResponse;
import com.guardianai.entity.AIAnalysis;
import com.guardianai.entity.User;
import com.guardianai.repository.AIAnalysisRepository;
import com.guardianai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private AIAnalysisRepository aiAnalysisRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AIService aiService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Amrutha", "amrutha@example.com", "pass", "9876543210", OffsetDateTime.now());
    }

    @Test
    @DisplayName("Should detect HIGH risk when danger or emergency keywords exist")
    void testEvaluateRiskHigh() {
        AIResponse response = aiService.evaluateRisk("Someone is following me and I am in immediate danger!");

        assertEquals("HIGH", response.getRiskLevel());
        assertTrue(response.getRecommendation().contains("Emergency SOS"));
    }

    @Test
    @DisplayName("Should detect MEDIUM risk for alone at night or uncomfortable scenarios")
    void testEvaluateRiskMedium() {
        AIResponse response = aiService.evaluateRisk("I am travelling alone at night and feel uncomfortable.");

        assertEquals("MEDIUM", response.getRiskLevel());
        assertTrue(response.getRecommendation().contains("Safety Check-In"));
    }

    @Test
    @DisplayName("Should detect LOW risk for normal situations")
    void testEvaluateRiskLow() {
        AIResponse response = aiService.evaluateRisk("Walking to the campus cafeteria with classmates.");

        assertEquals("LOW", response.getRiskLevel());
    }

    @Test
    @DisplayName("Should persist analysis in database and return assessment")
    void testAnalyzeSituationPersistence() {
        AIRequest request = new AIRequest("I feel worried because of a suspicious stranger near the bus stop");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        AIResponse response = aiService.analyzeSituation(1L, request);

        assertNotNull(response);
        assertEquals("MEDIUM", response.getRiskLevel());
        verify(aiAnalysisRepository, times(1)).save(any(AIAnalysis.class));
    }
}
