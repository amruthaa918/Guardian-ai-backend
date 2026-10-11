package com.guardianai.service;

import com.guardianai.dto.CheckInRequest;
import com.guardianai.dto.CheckInResponse;
import com.guardianai.entity.CheckIn;
import com.guardianai.entity.User;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.repository.CheckInRepository;
import com.guardianai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;

    public CheckInService(CheckInRepository checkInRepository, UserRepository userRepository) {
        this.checkInRepository = checkInRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CheckInResponse createCheckIn(Long userId, CheckInRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        CheckIn checkIn = new CheckIn();
        checkIn.setUser(user);
        checkIn.setCheckInTime(request.getCheckInTime());
        checkIn.setMessage(request.getMessage() != null ? request.getMessage().trim() : null);
        checkIn.setStatus("PENDING");

        CheckIn saved = checkInRepository.save(checkIn);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CheckInResponse> getCheckIns(Long userId) {
        return checkInRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CheckInResponse completeCheckIn(Long userId, Long checkInId) {
        CheckIn checkIn = checkInRepository.findByIdAndUserId(checkInId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in not found with id: " + checkInId));

        checkIn.setStatus("COMPLETED");
        CheckIn updated = checkInRepository.save(checkIn);
        return mapToResponse(updated);
    }

    private CheckInResponse mapToResponse(CheckIn checkIn) {
        return new CheckInResponse(
                checkIn.getId(),
                checkIn.getCheckInTime(),
                checkIn.getStatus(),
                checkIn.getMessage(),
                checkIn.getCreatedAt()
        );
    }
}
