package com.guardianai.service;

import com.guardianai.dto.EmergencyRequest;
import com.guardianai.dto.EmergencyResponse;
import com.guardianai.entity.EmergencyAlert;
import com.guardianai.entity.TrustedContact;
import com.guardianai.entity.User;
import com.guardianai.repository.EmergencyAlertRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @Mock
    private EmergencyAlertRepository emergencyAlertRepository;

    @Mock
    private TrustedContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EmergencyService emergencyService;

    private User sampleUser;
    private EmergencyAlert sampleAlert;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Amrutha", "amrutha@example.com", "pass", "9876543210", OffsetDateTime.now());
        sampleAlert = new EmergencyAlert(50L, sampleUser, "SOS", 17.3850, 78.4867, "ACTIVE", OffsetDateTime.now());
    }

    @Test
    @DisplayName("Should create active SOS alert and notify trusted contacts")
    void testTriggerSos() {
        EmergencyRequest request = new EmergencyRequest("SOS", 17.3850, 78.4867);
        TrustedContact contact = new TrustedContact(1L, sampleUser, "Father", "9876543210", "father@example.com", "Father", OffsetDateTime.now());

        com.guardianai.dto.ContactDeliveryStatus deliveryStatus = new com.guardianai.dto.ContactDeliveryStatus(
                1L, "Father", "9876543210", "father@example.com",
                "SENT", "SMS accepted by Twilio",
                "SENT", "Email sent successfully"
        );
        com.guardianai.dto.NotificationResult mockResult = new com.guardianai.dto.NotificationResult("SMS: 1 accepted, 0 failed.", List.of(deliveryStatus));

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(emergencyAlertRepository.save(any(EmergencyAlert.class))).thenReturn(sampleAlert);
        when(contactRepository.findByUserId(1L)).thenReturn(List.of(contact));
        when(notificationService.notifyTrustedContacts(eq(sampleUser), eq(sampleAlert), any())).thenReturn(mockResult);

        EmergencyResponse response = emergencyService.triggerSos(1L, request);

        assertNotNull(response);
        assertEquals("SOS alert created. SMS: 1 accepted, 0 failed.", response.getMessage());
        assertEquals(50L, response.getAlertId());
        assertEquals("ACTIVE", response.getStatus());
        assertNotNull(response.getContactDeliveryStatuses());
        assertEquals(1, response.getContactDeliveryStatuses().size());
        assertEquals("SENT", response.getContactDeliveryStatuses().get(0).getSmsStatus());
        assertEquals("Father", response.getContactDeliveryStatuses().get(0).getContactName());

        verify(notificationService, times(1)).notifyTrustedContacts(eq(sampleUser), eq(sampleAlert), any());
    }

    @Test
    @DisplayName("Should resolve emergency alert")
    void testResolveAlert() {
        when(emergencyAlertRepository.findByIdAndUserId(50L, 1L)).thenReturn(Optional.of(sampleAlert));
        when(emergencyAlertRepository.save(any(EmergencyAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmergencyResponse response = emergencyService.resolveAlert(1L, 50L);

        assertEquals("RESOLVED", response.getStatus());
        assertEquals("Emergency alert resolved successfully", response.getMessage());
    }
}
