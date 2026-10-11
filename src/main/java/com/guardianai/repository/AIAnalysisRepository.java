package com.guardianai.repository;

import com.guardianai.entity.AIAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, Long> {

    List<AIAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);
}
