package com.guardianai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianai.dto.ContactRequest;
import com.guardianai.dto.ContactResponse;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.security.CustomUserDetailsService;
import com.guardianai.security.JwtService;
import com.guardianai.security.UserPrincipal;
import com.guardianai.service.ContactService;
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
 * MockMvc controller tests for Trusted Contacts API.
 *
 * Uses @WebMvcTest to isolate the web layer.
 * JwtService and CustomUserDetailsService are mocked to avoid a DB context.
 * CSRF tokens are injected via SecurityMockMvcRequestPostProcessors.csrf()
 * because Spring Security CSRF protection is active in @WebMvcTest.
 */
@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    // Required by JwtAuthenticationFilter which is picked up with SecurityAutoConfiguration
    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal mockPrincipal;
    private ContactResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockPrincipal = new UserPrincipal(
                1L, "Amrutha", "amrutha@example.com", "hashedPassword",
                List.of(() -> "ROLE_USER")
        );
        sampleResponse = new ContactResponse(
                10L, "Father", "9988776655", "father@example.com", "Father", OffsetDateTime.now()
        );
    }

    // ── GET /api/contacts ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/contacts - returns contact list for authenticated user")
    void testGetContacts_Authenticated() throws Exception {
        when(contactService.getContacts(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/contacts")
                        .with(user(mockPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("[0].name").value("Father"))
                .andExpect(jsonPath("[0].phone").value("9988776655"))
                .andExpect(jsonPath("[0].email").value("father@example.com"))
                .andExpect(jsonPath("[0].relationship").value("Father"));
    }

    @Test
    @DisplayName("GET /api/contacts - returns 401 when no auth token provided")
    void testGetContacts_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/contacts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/contacts - returns empty list when user has no contacts")
    void testGetContacts_EmptyList() throws Exception {
        when(contactService.getContacts(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/contacts")
                        .with(user(mockPrincipal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── POST /api/contacts ────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/contacts - creates contact and returns 201 Created")
    void testCreateContact_Success() throws Exception {
        ContactRequest request = new ContactRequest("Father", "9988776655", "father@example.com", "Father");
        when(contactService.createContact(eq(1L), any(ContactRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/contacts")
                        .with(user(mockPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Father"))
                .andExpect(jsonPath("$.phone").value("9988776655"));
    }

    @Test
    @DisplayName("POST /api/contacts - returns 400 when name is blank")
    void testCreateContact_BlankName() throws Exception {
        ContactRequest badRequest = new ContactRequest("", "9988776655", "father@example.com", "Father");

        mockMvc.perform(post("/api/contacts")
                        .with(user(mockPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/contacts - returns 400 for invalid email format")
    void testCreateContact_InvalidEmail() throws Exception {
        ContactRequest badRequest = new ContactRequest("Father", "9988776655", "not-an-email", "Father");

        mockMvc.perform(post("/api/contacts")
                        .with(user(mockPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/contacts - returns 401 when unauthenticated")
    void testCreateContact_Unauthenticated() throws Exception {
        ContactRequest request = new ContactRequest("Father", "9988776655", "father@example.com", "Father");

        // Without user() — should fail with 401 (or 403 if CSRF fires first, which is acceptable)
        mockMvc.perform(post("/api/contacts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/contacts/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/contacts/{id} - updates contact and returns 200 OK")
    void testUpdateContact_Success() throws Exception {
        ContactRequest request = new ContactRequest("Dad Updated", "9911223344", "dad@example.com", "Father");
        ContactResponse updated = new ContactResponse(10L, "Dad Updated", "9911223344", "dad@example.com", "Father", OffsetDateTime.now());
        when(contactService.updateContact(eq(1L), eq(10L), any(ContactRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/contacts/10")
                        .with(user(mockPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dad Updated"))
                .andExpect(jsonPath("$.phone").value("9911223344"));
    }

    @Test
    @DisplayName("PUT /api/contacts/{id} - returns 404 when contact not found")
    void testUpdateContact_NotFound() throws Exception {
        ContactRequest request = new ContactRequest("Someone", "1234567890", "s@ex.com", "Friend");
        when(contactService.updateContact(eq(1L), eq(999L), any(ContactRequest.class)))
                .thenThrow(new ResourceNotFoundException("Contact not found with id: 999"));

        mockMvc.perform(put("/api/contacts/999")
                        .with(user(mockPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found with id: 999"));
    }

    @Test
    @DisplayName("PUT /api/contacts/{id} - returns 401 when unauthenticated")
    void testUpdateContact_Unauthenticated() throws Exception {
        ContactRequest request = new ContactRequest("Father", "9988776655", "father@example.com", "Father");

        mockMvc.perform(put("/api/contacts/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── DELETE /api/contacts/{id} ─────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/contacts/{id} - deletes contact and returns 204 No Content")
    void testDeleteContact_Success() throws Exception {
        doNothing().when(contactService).deleteContact(1L, 10L);

        mockMvc.perform(delete("/api/contacts/10")
                        .with(user(mockPrincipal))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(contactService, times(1)).deleteContact(1L, 10L);
    }

    @Test
    @DisplayName("DELETE /api/contacts/{id} - returns 404 when contact not found")
    void testDeleteContact_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Contact not found with id: 999"))
                .when(contactService).deleteContact(1L, 999L);

        mockMvc.perform(delete("/api/contacts/999")
                        .with(user(mockPrincipal))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found with id: 999"));
    }

    @Test
    @DisplayName("DELETE /api/contacts/{id} - returns 401 when unauthenticated")
    void testDeleteContact_Unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/contacts/10")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}