package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record MapInitUser(
    @JsonProperty("user_id") Long userId,
    @JsonProperty("nickname") String nickname,
    @JsonProperty("skill_level") Integer skillLevel,
    @JsonProperty("primary_vulnerability_type_id") Integer primaryVulnerabilityTypeId
) {
}

