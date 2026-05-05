package com.driving.backend.dto.admin;

import com.driving.backend.dto.user.VulnerabilityTypeDetail;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserSummaryResponse(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("email") String email,
        @JsonProperty("nickname") String nickname,
        @JsonProperty("skill_level") Integer skillLevel,
        @JsonProperty("primary_vulnerability_type_id") Integer primaryVulnerabilityTypeId,
        @JsonProperty("vulnerability_type_ids") List<Integer> vulnerabilityTypeIds,
        @JsonProperty("vulnerability_types") List<VulnerabilityTypeDetail> vulnerabilityTypes,
        @JsonProperty("survey_history_count") int surveyHistoryCount,
        @JsonProperty("latest_survey_at") LocalDateTime latestSurveyAt,
        @JsonProperty("created_at") LocalDateTime createdAt
) {
}
