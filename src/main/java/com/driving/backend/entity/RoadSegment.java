package com.driving.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "road_segments", indexes = {
        @Index(name = "idx_level", columnList = "level"),
        @Index(name = "idx_level_rule_id", columnList = "level_rule_id"),
        @Index(name = "idx_highway", columnList = "highway")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadSegment {

    @Id
    @Column(name = "segment_id", length = 50)
    private String segmentId;

    // ── 지도/도로 기본 정보 ──

    @Column(length = 120)
    private String name;

    @Column(length = 30)
    private String highway;

    @Column(name = "start_lat", nullable = false)
    private Double startLat;

    @Column(name = "start_lon", nullable = false)
    private Double startLon;

    @Column(name = "end_lat", nullable = false)
    private Double endLat;

    @Column(name = "end_lon", nullable = false)
    private Double endLon;

    @Column(name = "center_lat")
    private Double centerLat;

    @Column(name = "center_lon")
    private Double centerLon;

    @Column(name = "num_points")
    private Integer numPoints;

    @Column(name = "coordinates_json", nullable = false, columnDefinition = "JSON")
    private String coordinatesJson;

    // ── 난이도 점수 (구 segment_score 통합) ──

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

    // ── Hover/Click 설명 ──

    @Column(length = 300)
    private String explanation;

    @Column(name = "detail_description", columnDefinition = "TEXT")
    private String detailDescription;

    // ── 원천 근거 (향후 사용) ──

    @Column(name = "evidence_json", columnDefinition = "JSON")
    private String evidenceJson;

    // ── Level 결과 (구 segment_level 통합) ──

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_rule_id", nullable = false)
    private LevelRule levelRule;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "level_score")
    private Double levelScore;

    // ── 시각 ──

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
