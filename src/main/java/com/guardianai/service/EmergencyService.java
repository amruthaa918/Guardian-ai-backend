package com.guardianai.service;

import com.guardianai.dto.EmergencyRequest;
import com.guardianai.dto.EmergencyResponse;
import com.guardianai.entity.EmergencyAlert;
import com.guardianai.entity.TrustedContact;
import com.guardianai.entity.User;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.repository.EmergencyAlertRepository;
import com.guardianai.repository.TrustedContactRepository;
import com.guardianai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmergencyService {

    private final EmergencyAlertRepository emergencyAlertRepository;
    private final TrustedContactRepository contactRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public EmergencyService(EmergencyAlertRepository emergencyAlertRepository,
                            TrustedContactRepository contactRepository,
                            UserRepository userRepository,
                            NotificationService notificationService) {
        this.emergencyAlertRepository = emergencyAlertRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public EmergencyResponse triggerSos(Long userId, EmergencyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        EmergencyAlert alert = new EmergencyAlert();
        alert.setUser(user);
        alert.setAlertType(request.getAlertType() != null ? request.getAlertType().trim() : "SOS");
        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        alert.setStatus("ACTIVE");

        EmergencyAlert saved = emergencyAlertRepository.save(alert);

        List<TrustedContact> contacts = contactRepository.findByUserId(userId);
        com.guardianai.dto.NotificationResult notificationResult = notificationService.notifyTrustedContacts(user, saved, contacts);

        EmergencyResponse response = new EmergencyResponse(
                "SOS alert created. " + notificationResult.getSummaryMessage(),
                saved.getId(),
                saved.getStatus(),
                notificationResult.getContactStatuses()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<EmergencyResponse> getEmergencyHistory(Long userId) {
        return emergencyAlertRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(alert -> new EmergencyResponse(
                        alert.getId(),
                        alert.getAlertType(),
                        alert.getLatitude(),
                        alert.getLongitude(),
                        alert.getStatus(),
                        alert.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public EmergencyResponse resolveAlert(Long userId, Long alertId) {
        EmergencyAlert alert = emergencyAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency alert not found with id: " + alertId));

        alert.setStatus("RESOLVED");
        EmergencyAlert updated = emergencyAlertRepository.save(alert);

        return new EmergencyResponse("Emergency alert resolved successfully", updated.getId(), updated.getStatus());
    }
}
