package com.guardianai.service;

import com.guardianai.dto.CheckInRequest;
import com.guardianai.dto.CheckInResponse;
import com.guardianai.entity.CheckIn;
import com.guardianai.entity.User;
import com.guardianai.repository.CheckInRepository;
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
class CheckInServiceTest {

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CheckInService checkInService;

    private User sampleUser;
    private CheckIn sampleCheckIn;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Amrutha", "amrutha@example.com", "pass", "9876543210", OffsetDateTime.now());
        sampleCheckIn = new CheckIn(200L, sampleUser, OffsetDateTime.now().plusHours(2), "PENDING", "Home by 8 PM", OffsetDateTime.now());
    }

    @Test
    @DisplayName("Should create check-in with PENDING status")
    void testCreateCheckIn() {
        CheckInRequest request = new CheckInRequest(OffsetDateTime.now().plusHours(2), "Home by 8 PM");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(checkInRepository.save(any(CheckIn.class))).thenReturn(sampleCheckIn);

        CheckInResponse response = checkInService.createCheckIn(1L, request);

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals("Home by 8 PM", response.getMessage());
    }

    @Test
    @DisplayName("Should complete safety check-in")
    void testCompleteCheckIn() {
        when(checkInRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(sampleCheckIn));
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckInResponse response = checkInService.completeCheckIn(1L, 200L);

        assertEquals("COMPLETED", response.getStatus());
    }
}
