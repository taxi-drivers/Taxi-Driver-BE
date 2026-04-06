package com.driving.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OSM 경로 탐색 결과")
public class RouteResult {

    @Schema(description = "총 거리(m)", example = "3150")
    private long totalDistanceM;
    @Schema(description = "예상 소요 시간(분)", example = "9")
    private int estimatedMinutes;
    @Schema(description = "평균 난이도 점수", example = "31.4")
    private double avgDifficulty;
    private List<RouteSegment> segments;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "경로를 구성하는 개별 도로 조각")
    public static class RouteSegment {
        @Schema(description = "그래프 edge ID", example = "E_1024")
        private String edgeId;
        @Schema(description = "도로명", example = "테헤란로")
        private String name;
        @Schema(description = "OSM 도로 타입", example = "primary")
        private String highway;
        @Schema(description = "구간 길이(m)", example = "220.5")
        private double lengthM;
        @Schema(description = "구간 난이도", example = "34.8")
        private double difficulty;
        @Schema(description = "구간 좌표 JSON 문자열", example = "[[127.0388,37.5009],[127.0392,37.5011]]")
        private String coordinatesJson;
    }
}
