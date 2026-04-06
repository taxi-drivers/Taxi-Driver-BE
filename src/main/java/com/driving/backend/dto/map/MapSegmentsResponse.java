package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "지도 세그먼트 목록 응답")
public record MapSegmentsResponse(
    @Schema(description = "반환된 세그먼트 개수", example = "25")
    @JsonProperty("count") int count,
    @JsonProperty("items") List<MapSegmentItem> items
) {
}

