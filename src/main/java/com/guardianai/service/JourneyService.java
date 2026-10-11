package com.guardianai.service;

import com.guardianai.dto.JourneyRequest;
import com.guardianai.dto.JourneyResponse;
import com.guardianai.entity.Journey;
import com.guardianai.entity.User;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.repository.JourneyRepository;
import com.guardianai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JourneyService {

    private final JourneyRepository journeyRepository;
    private final UserRepository userRepository;

    public JourneyService(JourneyRepository journeyRepository, UserRepository userRepository) {
        this.journeyRepository = journeyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public JourneyResponse createJourney(Long userId, JourneyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Journey journey = new Journey();
        journey.setUser(user);
        journey.setStartLocation(request.getStartLocation().trim());
        journey.setDestination(request.getDestination().trim());
        journey.setExpectedArrival(request.getExpectedArrival());
        journey.setStatus("ACTIVE");

        Journey saved = journeyRepository.save(journey);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<JourneyResponse> getJourneys(Long userId) {
        return journeyRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JourneyResponse getJourneyById(Long userId, Long journeyId) {
        Journey journey = journeyRepository.findByIdAndUserId(journeyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + journeyId));
        return mapToResponse(journey);
    }

    @Transactional
    public JourneyResponse completeJourney(Long userId, Long journeyId) {
        Journey journey = journeyRepository.findByIdAndUserId(journeyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + journeyId));

        if (!"ACTIVE".equals(journey.getStatus())) {
            throw new IllegalArgumentException(
                    "Journey cannot be completed: current status is '" + journey.getStatus() + "'");
        }

        journey.setStatus("COMPLETED");
        Journey updated = journeyRepository.save(journey);
        return mapToResponse(updated);
    }

    @Transactional
    public JourneyResponse cancelJourney(Long userId, Long journeyId) {
        Journey journey = journeyRepository.findByIdAndUserId(journeyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Journey not found with id: " + journeyId));

        if (!"ACTIVE".equals(journey.getStatus())) {
            throw new IllegalArgumentException(
                    "Journey cannot be cancelled: current status is '" + journey.getStatus() + "'");
        }

        journey.setStatus("CANCELLED");
        Journey updated = journeyRepository.save(journey);
        return mapToResponse(updated);
    }

    private JourneyResponse mapToResponse(Journey journey) {
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
