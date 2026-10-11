package com.guardianai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianai.dto.ContactDeliveryStatus;
import com.guardianai.dto.EmergencyRequest;
import com.guardianai.dto.EmergencyResponse;
import com.guardianai.security.CustomUserDetailsService;
import com.guardianai.security.JwtService;
import com.guardianai.security.UserPrincipal;
import com.guardianai.service.EmergencyService;
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

@WebMvcTest(EmergencyController.class)
class EmergencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmergencyService emergencyService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = new UserPrincipal(
                1L, "Amrutha", "amrutha@example.com", "pass",
                List.of(() -> "ROLE_USER")
        );
    }

    @Test
    @DisplayName("POST /api/emergency/sos - Should trigger SOS and return per-contact statuses")
    void testTriggerSos() throws Exception {
        EmergencyRequest request = new EmergencyRequest("SOS", 17.3850, 78.4867);

        ContactDeliveryStatus contactStatus = new ContactDeliveryStatus(
                1L, "Mom", "+919876543210", "mom@example.com",
                "SENT", "SMS accepted by Twilio (HTTP 201)",
                "SENT", "Email sent successfully"
        );

        EmergencyResponse response = new EmergencyResponse(
                "SOS alert created. SMS: 1 accepted, 0 failed.",
                100L,
                "ACTIVE",
                List.of(contactStatus)
        );

        when(emergencyService.triggerSos(eq(1L), any(EmergencyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/emergency/sos")
                        .with(user(mockPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertId").value(100L))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("SOS alert created. SMS: 1 accepted, 0 failed."))
                .andExpect(jsonPath("$.contactDeliveryStatuses[0].contactId").value(1L))
                .andExpect(jsonPath("$.contactDeliveryStatuses[0].contactName").value("Mom"))
                .andExpect(jsonPath("$.contactDeliveryStatuses[0].smsStatus").value("SENT"))
                .andExpect(jsonPath("$.contactDeliveryStatuses[0].smsDetails").value("SMS accepted by Twilio (HTTP 201)"));

        verify(emergencyService, times(1)).triggerSos(eq(1L), any(EmergencyRequest.class));
    }

    @Test
    @DisplayName("GET /api/emergency/history - Should return emergency history")
    void testGetEmergencyHistory() throws Exception {
        EmergencyResponse alert = new EmergencyResponse(100L, "SOS", 17.3850, 78.4867, "ACTIVE", OffsetDateTime.now());
        when(emergencyService.getEmergencyHistory(1L)).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/emergency/history")
                        .with(user(mockPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alertId").value(100L))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("PUT /api/emergency/{id}/resolve - Should resolve alert")
    void testResolveAlert() throws Exception {
        EmergencyResponse alert = new EmergencyResponse("Emergency alert resolved successfully", 100L, "RESOLVED");
        when(emergencyService.resolveAlert(1L, 100L)).thenReturn(alert);

        mockMvc.perform(put("/api/emergency/100/resolve")
                        .with(user(mockPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value(100L))
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
}
