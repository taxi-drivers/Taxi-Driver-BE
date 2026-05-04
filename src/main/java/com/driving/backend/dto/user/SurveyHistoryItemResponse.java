package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record SurveyHistoryItemResponse(
        @JsonProperty("survey_history_id") Long surveyHistoryId,
        @JsonProperty("survey_version") String surveyVersion,
        @JsonProperty("skill_level") Integer skillLevel,
        @JsonProperty("vulnerability_type_ids") List<Integer> vulnerabilityTypeIds,
        @JsonProperty("primary_vulnerability_type_id") Integer primaryVulnerabilityTypeId,
        @JsonProperty("answers") Map<String, Integer> answers,
        @JsonProperty("created_at") LocalDateTime createdAt
) {
}
