package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record SegmentScoreBreakdown(
    @JsonProperty("accident_rate_score") Double accidentRateScore,
    @JsonProperty("road_shape_score") Double roadShapeScore,
    @JsonProperty("road_scale_score") Double roadScaleScore,
    @JsonProperty("intersection_score") Double intersectionScore,
    @JsonProperty("traffic_volume_score") Double trafficVolumeScore
) {
}

