package com.driving.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record ErrorResponse(
    @JsonProperty("status") int status,
    @JsonProperty("message") String message
) {
}

