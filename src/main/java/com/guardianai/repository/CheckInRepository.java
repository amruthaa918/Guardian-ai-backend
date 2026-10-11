package com.guardianai.repository;

import com.guardianai.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CheckIn> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndStatus(Long userId, String status);
}
