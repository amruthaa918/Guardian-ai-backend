package com.guardianai.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "testSecretKeyWithSufficientLengthForHmacSha256Security123456");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    @DisplayName("Should generate a valid JWT token and correctly extract claims")
    void testGenerateAndExtractClaims() {
        String email = "test@guardianai.com";
        Long userId = 42L;
        String name = "Test User";

        String token = jwtService.generateToken(email, userId, name);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String extractedEmail = jwtService.extractEmail(token);
        assertEquals(email, extractedEmail);

        Long extractedUserId = jwtService.extractUserId(token);
        assertEquals(userId, extractedUserId);

        assertFalse(jwtService.isTokenExpired(token));
    }
}
