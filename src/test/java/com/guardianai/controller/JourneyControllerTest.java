package com.guardianai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianai.dto.JourneyRequest;
import com.guardianai.dto.JourneyResponse;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.security.CustomUserDetailsService;
import com.guardianai.security.JwtService;
import com.guardianai.security.UserPrincipal;
import com.guardianai.service.JourneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc controller tests for Journey Mode API.
 * All 5 endpoints: POST create, GET list, GET by-id, PUT complete, PUT cancel.
 *
 * Security:
 *   - JwtService and CustomUserDetailsService are mocked (avoids DB context).
 *   - csrf() is added to all state-changing requests.
 *   - user(mockPrincipal) simulates a JWT-authenticated session.
 */
@WebMvcTest(JourneyController.class)
class JourneyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JourneyService journeyService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal mockPrincipal;
    private JourneyResponse activeResponse;
    private JourneyResponse completedResponse;
    private JourneyResponse cancelledResponse;

    @BeforeEach
    void setUp() {
        mockPrincipal = new UserPrincipal(
                1L, "Amrutha", "amrutha@example.com", "hashed",
                List.of(() -> "ROLE_USER")
        );

        OffsetDateTime now = OffsetDateTime.now();
        activeResponse    = new JourneyResponse(100L, "Hyderabad", "Secunderabad", now.plusHours(1), "ACTIVE",    now);
        completedResponse = new JourneyResponse(100L, "Hyderabad", "Secunderabad", now.plusHours(1), "COMPLETED", now);
        cancelledResponse = new JourneyResponse(100L, "Hyderabad", "Secunderabad", now.plusHours(1), "CANCELLED", now);
    }

    // ── POST /api/journeys ────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/journeys - creates journey and returns 201 Created")
    void testCreateJourney_Success() throws Exception {
        JourneyRequest request = new JourneyRequest("Hyderabad", "Secunderabad", OffsetDateTime.now().plusHours(1));
        when(journeyService.createJourney(eq(1L), any(JourneyRequest.class))).thenReturn(activeResponse);

        mockMvc.perform(post("/api/journeys")
                        .with(user(mockPrincipal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.startLocation").value("Hyderabad"))
                .andExpect(jsonPath("$.destination").value("Secunderabad"));
    }

    @Test
    @DisplayName("POST /api/journeys - returns 400 when startLocation is blank")
    void testCreateJourney_BlankStartLocation() throws Exception {
        JourneyRequest bad = new JourneyRequest("", "Secunderabad", null);

        mockMvc.perform(post("/api/journeys")
                        .with(user(mockPrincipal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/journeys - returns 400 when destination is blank")
    void testCreateJourney_BlankDestination() throws Exception {
        JourneyRequest bad = new JourneyRequest("Hyderabad", "", null);

        mockMvc.perform(post("/api/journeys")
                        .with(user(mockPrincipal)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/journeys - returns 401 when unauthenticated")
    void testCreateJourney_Unauthenticated() throws Exception {
        JourneyRequest request = new JourneyRequest("Hyderabad", "Secunderabad", null);

        mockMvc.perform(post("/api/journeys")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/journeys ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/journeys - returns journey list for authenticated user")
    void testGetJourneys_Authenticated() throws Exception {
        when(journeyService.getJourneys(1L)).thenReturn(List.of(activeResponse, completedResponse));

        mockMvc.perform(get("/api/journeys")
                        .with(user(mockPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("[0].status").value("ACTIVE"))
                .andExpect(jsonPath("[1].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/journeys - returns empty list when user has no journeys")
    void testGetJourneys_EmptyList() throws Exception {
        when(journeyService.getJourneys(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/journeys")
                        .with(user(mockPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/journeys - returns 401 when unauthenticated")
    void testGetJourneys_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/journeys")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/journeys/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/journeys/{id} - returns journey for valid owner")
    void testGetJourneyById_Success() throws Exception {
        when(journeyService.getJourneyById(1L, 100L)).thenReturn(activeResponse);

        mockMvc.perform(get("/api/journeys/100")
                        .with(user(mockPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.startLocation").value("Hyderabad"));
    }

    @Test
    @DisplayName("GET /api/journeys/{id} - returns 404 when journey not found or other user's")
    void testGetJourneyById_NotFound() throws Exception {
        when(journeyService.getJourneyById(1L, 999L))
                .thenThrow(new ResourceNotFoundException("Journey not found with id: 999"));

        mockMvc.perform(get("/api/journeys/999")
                        .with(user(mockPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Journey not found with id: 999"));
    }

    @Test
    @DisplayName("GET /api/journeys/{id} - returns 401 when unauthenticated")
    void testGetJourneyById_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/journeys/100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/journeys/{id}/complete ───────────────────────────────────────

    @Test
    @DisplayName("PUT /api/journeys/{id}/complete - marks journey COMPLETED and returns 200")
    void testCompleteJourney_Success() throws Exception {
        when(journeyService.completeJourney(1L, 100L)).thenReturn(completedResponse);

        mockMvc.perform(put("/api/journeys/100/complete")
                        .with(user(mockPrincipal)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("PUT /api/journeys/{id}/complete - returns 404 when journey not found")
    void testCompleteJourney_NotFound() throws Exception {
        when(journeyService.completeJourney(1L, 999L))
                .thenThrow(new ResourceNotFoundException("Journey not found with id: 999"));

        mockMvc.perform(put("/api/journeys/999/complete")
                        .with(user(mockPrincipal)).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Journey not found with id: 999"));
    }

    @Test
    @DisplayName("PUT /api/journeys/{id}/complete - returns 400 when journey not ACTIVE")
    void testCompleteJourney_NotActive() throws Exception {
        when(journeyService.completeJourney(1L, 101L))
                .thenThrow(new IllegalArgumentException("Journey cannot be completed: current status is 'COMPLETED'"));

        mockMvc.perform(put("/api/journeys/101/complete")
                        .with(user(mockPrincipal)).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/journeys/{id}/complete - returns 401 when unauthenticated")
    void testCompleteJourney_Unauthenticated() throws Exception {
        mockMvc.perform(put("/api/journeys/100/complete")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/journeys/{id}/cancel ─────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/journeys/{id}/cancel - marks journey CANCELLED and returns 200")
    void testCancelJourney_Success() throws Exception {
        when(journeyService.cancelJourney(1L, 100L)).thenReturn(cancelledResponse);

        mockMvc.perform(put("/api/journeys/100/cancel")
                        .with(user(mockPrincipal)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PUT /api/journeys/{id}/cancel - returns 404 when journey not found")
    void testCancelJourney_NotFound() throws Exception {
        when(journeyService.cancelJourney(1L, 999L))
                .thenThrow(new ResourceNotFoundException("Journey not found with id: 999"));

        mockMvc.perform(put("/api/journeys/999/cancel")
                        .with(user(mockPrincipal)).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Journey not found with id: 999"));
    }

    @Test
    @DisplayName("PUT /api/journeys/{id}/cancel - returns 400 when journey not ACTIVE")
    void testCancelJourney_NotActive() throws Exception {
        when(journeyService.cancelJourney(1L, 102L))
                .thenThrow(new IllegalArgumentException("Journey cannot be cancelled: current status is 'CANCELLED'"));

        mockMvc.perform(put("/api/journeys/102/cancel")
                        .with(user(mockPrincipal)).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/journeys/{id}/cancel - returns 401 when unauthenticated")
    void testCancelJourney_Unauthenticated() throws Exception {
        mockMvc.perform(put("/api/journeys/100/cancel")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}