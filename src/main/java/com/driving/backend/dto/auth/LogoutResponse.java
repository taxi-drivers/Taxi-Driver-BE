package com.driving.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record LogoutResponse(
    @JsonProperty("message") String message
) {
}

