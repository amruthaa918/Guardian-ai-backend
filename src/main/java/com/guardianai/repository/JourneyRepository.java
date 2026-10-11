package com.guardianai.repository;

import com.guardianai.entity.Journey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JourneyRepository extends JpaRepository<Journey, Long> {

    List<Journey> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Journey> findByIdAndUserId(Long id, Long userId);

    Optional<Journey> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
}
