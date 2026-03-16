package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record MapSegmentsResponse(
    @JsonProperty("count") int count,
    @JsonProperty("items") List<MapSegmentItem> items
) {
}

