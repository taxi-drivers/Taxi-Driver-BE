package com.driving.backend.dto;

import com.driving.backend.entity.RoadSegment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SegmentTooltipResponse {

    private String segmentId;
    private String name;
    private Integer level;
    private String levelText;
    private Double totalScore;
    private String explanation;

    public static SegmentTooltipResponse from(RoadSegment segment) {
        return SegmentTooltipResponse.builder()
                .segmentId(segment.getSegmentId())
                .name(segment.getName())
                .level(segment.getLevel())
                .levelText(segment.getLevelText())
                .totalScore(segment.getTotalScore())
                .explanation(segment.getExplanation())
                .build();
    }
}
