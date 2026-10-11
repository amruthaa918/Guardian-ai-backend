package com.guardianai.repository;

import com.guardianai.entity.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {

    List<EmergencyAlert> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<EmergencyAlert> findByIdAndUserId(Long id, Long userId);

    Optional<EmergencyAlert> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    Optional<EmergencyAlert> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
