package com.driving.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record LoginResponse(
    @JsonProperty("user_id") Long userId,
    @JsonProperty("nickname") String nickname,
    @JsonProperty("skill_level") Integer skillLevel,
    @JsonProperty("vulnerability_type_id") Integer vulnerabilityTypeId,
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
) {
}

