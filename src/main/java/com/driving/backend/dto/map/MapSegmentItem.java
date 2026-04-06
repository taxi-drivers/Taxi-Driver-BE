package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "지도에 렌더링할 세그먼트")
public record MapSegmentItem(
    @Schema(description = "세그먼트 ID", example = "SEG_000001")
    @JsonProperty("segment_id") String segmentId,
    @Schema(description = "도로명", example = "테헤란로", nullable = true)
    @JsonProperty("name") String name,
    @Schema(description = "OSM 도로 타입", example = "primary")
    @JsonProperty("highway") String highway,
    @JsonProperty("center") SegmentCenter center,
    @JsonProperty("coordinates") List<CoordinatePoint> coordinates,
    @Schema(description = "난이도 레벨", example = "2")
    @JsonProperty("level") Integer level,
    @Schema(description = "난이도 텍스트", example = "보통")
    @JsonProperty("level_text") String levelText,
    @Schema(description = "종합 점수", example = "34.8")
    @JsonProperty("total_score") Double totalScore,
    @Schema(description = "툴팁 요약 설명", example = "차선 수와 교차로 복잡도가 반영됨")
    @JsonProperty("explanation") String explanation,
    @Schema(description = "점수 계산 시각", example = "2026-03-21T22:00:00")
    @JsonProperty("computed_at") LocalDateTime computedAt
) {
}

