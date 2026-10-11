package com.guardianai.service;

import com.guardianai.dto.DashboardResponse;
import com.guardianai.entity.Journey;
import com.guardianai.entity.User;
import com.guardianai.repository.CheckInRepository;
import com.guardianai.repository.EmergencyAlertRepository;
import com.guardianai.repository.JourneyRepository;
import com.guardianai.repository.TrustedContactRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JourneyRepository journeyRepository;

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private TrustedContactRepository trustedContactRepository;

    @Mock
    private EmergencyAlertRepository emergencyAlertRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Amrutha", "amrutha@example.com", "pass", "9876543210", OffsetDateTime.now());
    }

    @Test
    @DisplayName("Should return accurate dashboard summary metrics")
    void testGetDashboardSummary() {
        Journey activeJourney = new Journey(1L, sampleUser, "Home", "Campus", OffsetDateTime.now().plusHours(1), "ACTIVE", OffsetDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(journeyRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE")).thenReturn(Optional.of(activeJourney));
        when(checkInRepository.countByUserIdAndStatus(1L, "PENDING")).thenReturn(2L);
        when(trustedContactRepository.countByUserId(1L)).thenReturn(3L);
        when(emergencyAlertRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(1L, "ACTIVE")).thenReturn(Optional.empty());
        when(emergencyAlertRepository.findFirstByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());

        DashboardResponse summary = dashboardService.getDashboardSummary(1L);

        assertNotNull(summary);
        assertEquals("Amrutha", summary.getUserName());
        assertEquals("SAFE", summary.getSafetyStatus());
        assertNotNull(summary.getActiveJourney());
        assertEquals(2L, summary.getPendingCheckIns());
        assertEquals(3L, summary.getTrustedContactsCount());
        assertNull(summary.getLatestEmergency());
    }
}
