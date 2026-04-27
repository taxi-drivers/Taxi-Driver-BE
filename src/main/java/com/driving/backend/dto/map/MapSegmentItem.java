package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record MapSegmentItem(
    @JsonProperty("segment_id") String segmentId,
    @JsonProperty("name") String name,
    @JsonProperty("highway") String highway,
    @JsonProperty("center") SegmentCenter center,
    @JsonProperty("coordinates") List<CoordinatePoint> coordinates,
    @JsonProperty("level") Integer level,
    @JsonProperty("level_text") String levelText,
    @JsonProperty("total_score") Double totalScore,
    @JsonProperty("explanation") String explanation,
    @JsonProperty("computed_at") LocalDateTime computedAt
) {
}

