package com.driving.backend.dto;

import com.driving.backend.entity.RoadSegment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SegmentDifficultyResponse {

    private String segmentId;
    private Integer level;
    private String levelText;
    private Double totalScore;

    public static SegmentDifficultyResponse from(RoadSegment segment) {
        return SegmentDifficultyResponse.builder()
                .segmentId(segment.getSegmentId())
                .level(segment.getLevel())
                .levelText(segment.getLevelText())
                .totalScore(segment.getTotalScore())
                .build();
    }
}
