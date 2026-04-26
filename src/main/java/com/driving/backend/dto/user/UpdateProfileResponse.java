package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record UpdateProfileResponse(
    @JsonProperty("user_id") Long userId,
    @JsonProperty("email") String email,
    @JsonProperty("nickname") String nickname,
    @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}

