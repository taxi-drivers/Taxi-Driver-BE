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
    }
}
