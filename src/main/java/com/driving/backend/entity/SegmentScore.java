package com.driving.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "segment_score")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentScore {

    @Id
    @Column(name = "segment_id", length = 50)
    private String segmentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "segment_id")
    private RoadSegment roadSegment;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "level_text", length = 20)
    private String levelText;

    @Column(name = "accident_rate_score", nullable = false)
    private Double accidentRateScore;

    @Column(name = "road_shape_score", nullable = false)
    private Double roadShapeScore;

    @Column(name = "road_scale_score", nullable = false)
    private Double roadScaleScore;

    @Column(name = "intersection_score", nullable = false)
    private Double intersectionScore;

    @Column(name = "traffic_volume_score", nullable = false)
    private Double trafficVolumeScore;

    @Column(length = 300)
    private String explanation;

    @Column(name = "detail_description", columnDefinition = "TEXT")
    private String detailDescription;

    @Column(name = "detail_source", length = 50)
    private String detailSource;

    @Column(name = "detail_source_ref", length = 200)
    private String detailSourceRef;

    @Column(name = "detail_updated_at")
    private LocalDateTime detailUpdatedAt;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;
}
