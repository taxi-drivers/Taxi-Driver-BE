package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record CoordinatePoint(
    @JsonProperty("lat") double lat,
    @JsonProperty("lon") double lon
) {
}

