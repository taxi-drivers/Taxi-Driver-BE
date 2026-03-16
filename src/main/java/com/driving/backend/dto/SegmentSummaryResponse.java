package com.driving.backend.dto;

import com.driving.backend.entity.RoadSegment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SegmentSummaryResponse {

    private String segmentId;
    private String name;
    private Double centerLat;
    private Double centerLon;
    private Integer level;
    private String levelText;
    private Double totalScore;

    public static SegmentSummaryResponse from(RoadSegment segment) {
        return SegmentSummaryResponse.builder()
                .segmentId(segment.getSegmentId())
                .name(segment.getName())
                .centerLat(segment.getCenterLat())
                .centerLon(segment.getCenterLon())
                .level(segment.getLevel())
                .levelText(segment.getLevelText())
                .totalScore(segment.getTotalScore())
                .build();
    }
}
