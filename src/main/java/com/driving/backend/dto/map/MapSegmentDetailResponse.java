package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record MapSegmentDetailResponse(
    @JsonProperty("segment_id") String segmentId,
    @JsonProperty("name") String name,
    @JsonProperty("highway") String highway,
    @JsonProperty("level") Integer level,
    @JsonProperty("level_text") String levelText,
    @JsonProperty("total_score") Double totalScore,
    @JsonProperty("detail_title") String detailTitle,
    @JsonProperty("detail_description") String detailDescription,
    @JsonProperty("score_breakdown") SegmentScoreBreakdown scoreBreakdown,
    @JsonProperty("evidence") List<SegmentEvidenceItem> evidence,
    @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}

