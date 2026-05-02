package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record SubmitSurveyResponse(
    @JsonProperty("survey_history_id") Long surveyHistoryId,
    @JsonProperty("user_id") Long userId,
    @JsonProperty("skill_level") Integer skillLevel,
    @JsonProperty("vulnerability_type_ids") List<Integer> vulnerabilityTypeIds,
    @JsonProperty("primary_vulnerability_type_id") Integer primaryVulnerabilityTypeId,
    @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}

