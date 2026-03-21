package com.driving.backend.dto;

import com.driving.backend.entity.RoadSegment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SegmentScoreDetailResponse {

    private String segmentId;
    private Double totalScore;
    private Double accidentRateScore;
    private Double roadShapeScore;
    private Double roadScaleScore;
    private Double intersectionScore;
    private Double trafficVolumeScore;

    public static SegmentScoreDetailResponse from(RoadSegment segment) {
        return SegmentScoreDetailResponse.builder()
                .segmentId(segment.getSegmentId())
                .totalScore(segment.getTotalScore())
                .accidentRateScore(segment.getAccidentRateScore())
                .roadShapeScore(segment.getRoadShapeScore())
                .roadScaleScore(segment.getRoadScaleScore())
                .intersectionScore(segment.getIntersectionScore())
                .trafficVolumeScore(segment.getTrafficVolumeScore())
                .build();
    }
}
