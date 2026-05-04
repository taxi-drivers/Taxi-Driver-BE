package com.driving.backend.repository;

import com.driving.backend.entity.UserSurveyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSurveyHistoryRepository extends JpaRepository<UserSurveyHistory, Long> {

    List<UserSurveyHistory> findByUserUserIdOrderByCreatedAtDesc(Long userId);
}
