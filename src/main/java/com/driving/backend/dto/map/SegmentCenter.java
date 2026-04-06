package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세그먼트 중심 좌표")
public record SegmentCenter(
    @Schema(description = "위도", example = "37.501")
    @JsonProperty("lat") double lat,
    @Schema(description = "경도", example = "127.039")
    @JsonProperty("lon") double lon
) {
}

