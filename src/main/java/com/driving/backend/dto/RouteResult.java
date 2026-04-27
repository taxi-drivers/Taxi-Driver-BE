package com.driving.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResult {

    private long totalDistanceM;
    private int estimatedMinutes;
    private double avgDifficulty;
    private List<RouteSegment> segments;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteSegment {
        private String edgeId;
        private String name;
        private String highway;
        private double lengthM;
        private double difficulty;
        private String coordinatesJson;

        // 5개 세부 점수 (모달에 분해해서 표시)
        private Double accidentRateScore;
        private Double roadShapeScore;
        private Double roadScaleScore;
        private Double intersectionScore;
        private Double trafficVolumeScore;

        // 경사도 (signed fraction, null 가능)
        private Double slope;
    }
}
