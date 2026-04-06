package com.driving.backend.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "OSM 경로 탐색 요청")
public record RouteSearchRequest(
        @Schema(description = "출발지 위도", example = "37.4979", requiredMode = Schema.RequiredMode.REQUIRED)
        Double startLat,
        @Schema(description = "출발지 경도", example = "127.0276", requiredMode = Schema.RequiredMode.REQUIRED)
        Double startLon,
        @Schema(description = "도착지 위도", example = "37.5172", requiredMode = Schema.RequiredMode.REQUIRED)
        Double endLat,
        @Schema(description = "도착지 경도", example = "127.0473", requiredMode = Schema.RequiredMode.REQUIRED)
        Double endLon,
        @ArraySchema(
                arraySchema = @Schema(description = "직접 지정할 취약특성 코드 목록. 비우면 로그인 사용자의 설문 결과를 자동 사용"),
                schema = @Schema(example = "AVOID_HIGHWAY")
        )
        List<String> vulnerabilities
) {
}
