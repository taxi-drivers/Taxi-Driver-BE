package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사용자 취약 세그먼트 목록 응답")
public record VulnerableSegmentsResponse(
    @Schema(description = "사용자 ID", example = "1")
    @JsonProperty("user_id") Long userId,
    @Schema(description = "조회에 사용된 취약특성 ID 목록", example = "[1,3]")
    @JsonProperty("vulnerability_type_ids") List<Integer> vulnerabilityTypeIds,
    @Schema(description = "반환 개수", example = "3")
    @JsonProperty("count") int count,
    @JsonProperty("items") List<VulnerableSegmentItem> items
) {
}

