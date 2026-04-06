package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record SubmitSurveyRequest(
    @JsonProperty("skill_level") Integer skillLevel,
    @JsonProperty("vulnerability_type_ids") List<Integer> vulnerabilityTypeIds,
    @JsonProperty("primary_vulnerability_type_id") Integer primaryVulnerabilityTypeId,
    @JsonProperty("answers") Map<String, Integer> answers,
    @JsonProperty("client_version") String clientVersion
) {
}

