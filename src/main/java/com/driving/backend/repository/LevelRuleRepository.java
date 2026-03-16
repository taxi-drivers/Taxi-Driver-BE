package com.driving.backend.repository;

import com.driving.backend.entity.LevelRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Provides database access methods through Spring Data JPA.
 */
public interface LevelRuleRepository extends JpaRepository<LevelRule, Long> {
    Optional<LevelRule> findFirstByIsActiveTrueOrderByLevelRuleIdDesc();
}

