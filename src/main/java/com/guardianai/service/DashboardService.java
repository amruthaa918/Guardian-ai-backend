package com.guardianai.service;

import com.guardianai.dto.DashboardResponse;
import com.guardianai.dto.EmergencyResponse;
import com.guardianai.dto.JourneyResponse;
import com.guardianai.entity.EmergencyAlert;
import com.guardianai.entity.Journey;
import com.guardianai.entity.User;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.repository.CheckInRepository;
import com.guardianai.repository.EmergencyAlertRepository;
import com.guardianai.repository.JourneyRepository;
import com.guardianai.repository.TrustedContactRepository;
import com.guardianai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final JourneyRepository journeyRepository;
    private final CheckInRepository checkInRepository;
    private final TrustedContactRepository trustedContactRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;

    public DashboardService(UserRepository userRepository,
                            JourneyRepository journeyRepository,
                            CheckInRepository checkInRepository,
                            TrustedContactRepository trustedContactRepository,
                            EmergencyAlertRepository emergencyAlertRepository) {
        this.userRepository = userRepository;
        this.journeyRepository = journeyRepository;
        this.checkInRepository = checkInRepository;
        this.trustedContactRepository = trustedContactRepository;
        this.emergencyAlertRepository = emergencyAlertRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Active journey
        JourneyResponse activeJourney = journeyRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE")
                .map(this::mapJourneyToResponse)
                .orElse(null);

        // Pending check-ins count
        long pendingCheckIns = checkInRepository.countByUserIdAndStatus(userId, "PENDING");

        // Trusted contacts count
        long contactsCount = trustedContactRepository.countByUserId(userId);

        // Active / Latest emergency
        Optional<EmergencyAlert> activeEmergencyOpt = emergencyAlertRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE");

        Optional<EmergencyAlert> latestEmergencyOpt = emergencyAlertRepository
                .findFirstByUserIdOrderByCreatedAtDesc(userId);

        EmergencyResponse latestEmergency = latestEmergencyOpt
                .map(alert -> new EmergencyResponse(
                        alert.getId(),
                        alert.getAlertType(),
                        alert.getLatitude(),
                        alert.getLongitude(),
                        alert.getStatus(),
                        alert.getCreatedAt()
                ))
                .orElse(null);

        // Compute overall safety status
        String safetyStatus = activeEmergencyOpt.isPresent() ? "ALERT" : "SAFE";

        return new DashboardResponse(
                user.getName(),
                safetyStatus,
                activeJourney,
                pendingCheckIns,
                contactsCount,
                latestEmergency
        );
    }

    private JourneyResponse mapJourneyToResponse(Journey journey) {
        return new JourneyResponse(
                journey.getId(),
                journey.getStartLocation(),
                journey.getDestination(),
                journey.getExpectedArrival(),
                journey.getStatus(),
                journey.getCreatedAt()
        );
    }
}
