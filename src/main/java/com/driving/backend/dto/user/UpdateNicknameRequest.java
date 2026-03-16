package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record UpdateNicknameRequest(
    @JsonProperty("nickname") String nickname
) {
}

