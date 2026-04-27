package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record UserProfileResponse(
    @JsonProperty("user_id") Long userId,
    @JsonProperty("email") String email,
    @JsonProperty("nickname") String nickname,
    @JsonProperty("skill_level") Integer skillLevel,
    @JsonProperty("primary_vulnerability_type_id") Integer primaryVulnerabilityTypeId,
    @JsonProperty("vulnerability_type_ids") List<Integer> vulnerabilityTypeIds,
    @JsonProperty("vulnerability_types") List<VulnerabilityTypeDetail> vulnerabilityTypes,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}

