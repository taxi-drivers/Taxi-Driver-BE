package com.driving.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record LoginResponse(
    @JsonProperty("user_id") Long userId,
    @JsonProperty("nickname") String nickname,
    @JsonProperty("skill_level") Integer skillLevel,
    @JsonProperty("primary_vulnerability_type_id") Integer vulnerabilityTypeId,
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("role") String role
) {
    /** 호환용 6-arg 생성자 — role 기본값 USER */
    public LoginResponse(
            Long userId, String nickname, Integer skillLevel,
            Integer vulnerabilityTypeId, String accessToken, String refreshToken
    ) {
        this(userId, nickname, skillLevel, vulnerabilityTypeId, accessToken, refreshToken, "USER");
    }
}
