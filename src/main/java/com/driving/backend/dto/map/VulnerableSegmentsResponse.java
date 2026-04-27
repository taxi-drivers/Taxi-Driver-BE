package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record VulnerableSegmentsResponse(
    @JsonProperty("user_id") Long userId,
    @JsonProperty("vulnerability_type_ids") List<Integer> vulnerabilityTypeIds,
    @JsonProperty("count") int count,
    @JsonProperty("items") List<VulnerableSegmentItem> items
) {
}

