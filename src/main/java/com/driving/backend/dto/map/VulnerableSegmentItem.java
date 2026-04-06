package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "사용자 취약특성에 해당하는 취약 세그먼트")
public record VulnerableSegmentItem(
    @Schema(description = "세그먼트 ID", example = "SEG_000001")
    @JsonProperty("segment_id") String segmentId,
    @Schema(description = "취약특성 ID", example = "1")
    @JsonProperty("vulnerability_type_id") Integer vulnerabilityTypeId,
    @Schema(description = "취약 강도", example = "0.8")
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

