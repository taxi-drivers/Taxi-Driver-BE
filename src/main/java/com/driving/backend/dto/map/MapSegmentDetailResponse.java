package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "지도 상세 패널 응답")
public record MapSegmentDetailResponse(
    @Schema(description = "세그먼트 ID", example = "SEG_000001")
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

