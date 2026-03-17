package com.driving.backend.dto;

import com.driving.backend.entity.RoadSegment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SegmentDetailResponse {

    private String segmentId;
    private String name;
    private String highway;

    // 좌표
    private Double startLat;
    private Double startLon;
    private Double endLat;
    private Double endLon;
    private Double centerLat;
    private Double centerLon;
    private String coordinatesJson;

    // 난이도
    private Integer level;
    private String levelText;
    private Double totalScore;

    // 설명
    private String explanation;
    private String detailDescription;

    private LocalDateTime computedAt;

    public static SegmentDetailResponse from(RoadSegment segment) {
        return SegmentDetailResponse.builder()
                .segmentId(segment.getSegmentId())
                .name(segment.getName())
                .highway(segment.getHighway())
                .startLat(segment.getStartLat())
                .startLon(segment.getStartLon())
                .endLat(segment.getEndLat())
                .endLon(segment.getEndLon())
                .centerLat(segment.getCenterLat())
                .centerLon(segment.getCenterLon())
                .coordinatesJson(segment.getCoordinatesJson())
                .level(segment.getLevel())
                .levelText(segment.getLevelText())
                .totalScore(segment.getTotalScore())
                .explanation(segment.getExplanation())
                .detailDescription(segment.getDetailDescription())
                .computedAt(segment.getComputedAt())
                .build();
    }
}
