package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "위경도 좌표")
public record CoordinatePoint(
    @Schema(description = "위도", example = "37.5009")
    @JsonProperty("lat") double lat,
    @Schema(description = "경도", example = "127.0388")
    @JsonProperty("lon") double lon
) {
}

