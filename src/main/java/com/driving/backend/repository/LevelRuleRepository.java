package com.driving.backend.repository;

import com.driving.backend.entity.LevelRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LevelRuleRepository extends JpaRepository<LevelRule, Long> {

    Optional<LevelRule> findByIsActiveTrue();

    Optional<LevelRule> findFirstByIsActiveTrueOrderByLevelRuleIdDesc();
}
