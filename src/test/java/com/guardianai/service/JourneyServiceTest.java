package com.guardianai.service;

import com.guardianai.dto.JourneyRequest;
import com.guardianai.dto.JourneyResponse;
import com.guardianai.entity.Journey;
import com.guardianai.entity.User;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.repository.JourneyRepository;
import com.guardianai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JourneyService.
 * Covers create, list, get-by-id, complete, cancel and their error paths.
 */
@ExtendWith(MockitoExtension.class)
class JourneyServiceTest {

    @Mock
    private JourneyRepository journeyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JourneyService journeyService;

    private User sampleUser;
    private Journey activeJourney;
    private Journey completedJourney;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Amrutha", "amrutha@example.com", "pass", "9876543210", OffsetDateTime.now());

        activeJourney = new Journey(
                100L, sampleUser,
                "Hyderabad", "Secunderabad",
                OffsetDateTime.now().plusHours(1),
                "ACTIVE", OffsetDateTime.now()
        );

        completedJourney = new Journey(
                101L, sampleUser,
                "Hyderabad", "Secunderabad",
                OffsetDateTime.now().plusHours(1),
                "COMPLETED", OffsetDateTime.now()
        );
    }

    // ── createJourney ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createJourney - should save journey with ACTIVE status")
    void testCreateJourney_Success() {
        JourneyRequest request = new JourneyRequest(
                "Hyderabad", "Secunderabad", OffsetDateTime.now().plusHours(1)
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(journeyRepository.save(any(Journey.class))).thenReturn(activeJourney);

        JourneyResponse response = journeyService.createJourney(1L, request);

        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("Hyderabad", response.getStartLocation());
        assertEquals("Secunderabad", response.getDestination());
        verify(journeyRepository, times(1)).save(any(Journey.class));
    }

    @Test
    @DisplayName("createJourney - should throw when user not found")
    void testCreateJourney_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> journeyService.createJourney(99L,
                        new JourneyRequest("A", "B", null))
        );
        verify(journeyRepository, never()).save(any());
    }

    // ── getJourneys ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getJourneys - should return all journeys for user ordered by createdAt desc")
    void testGetJourneys_ReturnsList() {
        when(journeyRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(activeJourney, completedJourney));

        List<JourneyResponse> result = journeyService.getJourneys(1L);

        assertEquals(2, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
        assertEquals("COMPLETED", result.get(1).getStatus());
    }

    @Test
    @DisplayName("getJourneys - should return empty list when user has no journeys")
    void testGetJourneys_EmptyList() {
        when(journeyRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        List<JourneyResponse> result = journeyService.getJourneys(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getJourneyById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getJourneyById - should return journey for valid owner")
    void testGetJourneyById_Success() {
        when(journeyRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(activeJourney));

        JourneyResponse response = journeyService.getJourneyById(1L, 100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Hyderabad", response.getStartLocation());
    }

    @Test
    @DisplayName("getJourneyById - should throw when journey not found or belongs to another user")
    void testGetJourneyById_NotFound() {
        when(journeyRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> journeyService.getJourneyById(1L, 999L)
        );
    }

    // ── completeJourney ───────────────────────────────────────────────────────

    @Test
    @DisplayName("completeJourney - should mark ACTIVE journey as COMPLETED")
    void testCompleteJourney_Success() {
        when(journeyRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(activeJourney));
        when(journeyRepository.save(any(Journey.class))).thenAnswer(inv -> inv.getArgument(0));

        JourneyResponse response = journeyService.completeJourney(1L, 100L);

        assertEquals("COMPLETED", response.getStatus());
        verify(journeyRepository, times(1)).save(any(Journey.class));
    }

    @Test
    @DisplayName("completeJourney - should throw when journey not found")
    void testCompleteJourney_NotFound() {
        when(journeyRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> journeyService.completeJourney(1L, 999L)
        );
    }

    @Test
    @DisplayName("completeJourney - should throw when journey is already COMPLETED")
    void testCompleteJourney_AlreadyCompleted() {
        when(journeyRepository.findByIdAndUserId(101L, 1L)).thenReturn(Optional.of(completedJourney));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> journeyService.completeJourney(1L, 101L)
        );
        assertTrue(ex.getMessage().contains("COMPLETED"));
    }

    // ── cancelJourney ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelJourney - should mark ACTIVE journey as CANCELLED")
    void testCancelJourney_Success() {
        when(journeyRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(activeJourney));
        when(journeyRepository.save(any(Journey.class))).thenAnswer(inv -> inv.getArgument(0));

        JourneyResponse response = journeyService.cancelJourney(1L, 100L);

        assertEquals("CANCELLED", response.getStatus());
    }

    @Test
    @DisplayName("cancelJourney - should throw when journey not found")
    void testCancelJourney_NotFound() {
        when(journeyRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> journeyService.cancelJourney(1L, 999L)
        );
    }

    @Test
    @DisplayName("cancelJourney - should throw when journey is already CANCELLED")
    void testCancelJourney_AlreadyCancelled() {
        Journey cancelledJourney = new Journey(
                102L, sampleUser, "A", "B", null, "CANCELLED", OffsetDateTime.now()
        );
        when(journeyRepository.findByIdAndUserId(102L, 1L)).thenReturn(Optional.of(cancelledJourney));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> journeyService.cancelJourney(1L, 102L)
        );
        assertTrue(ex.getMessage().contains("CANCELLED"));
    }
}