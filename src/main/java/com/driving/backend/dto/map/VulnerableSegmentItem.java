package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Defines request and response payload structures for API boundaries.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VulnerableSegmentItem(
    @JsonProperty("segment_id") String segmentId,
    @JsonProperty("vulnerability_type_id") Integer vulnerabilityTypeId,
    @JsonProperty("severity") Double severity,
    @JsonProperty("note") String note,
    @JsonProperty("source") String source,
    @JsonProperty("level") Integer level,
    @JsonProperty("level_text") String levelText,
    @JsonProperty("total_score") Double totalScore,
    @JsonProperty("explanation") String explanation,
    @JsonProperty("center") SegmentCenter center,
    @JsonProperty("coordinates") List<CoordinatePoint> coordinates
) {
}

