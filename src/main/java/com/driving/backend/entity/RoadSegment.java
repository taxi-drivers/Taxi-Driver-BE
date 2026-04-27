package com.driving.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "evidence_json", columnDefinition = "JSON")
    private String evidenceJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_rule_id", nullable = false)
    private LevelRule levelRule;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "level_score")
    private Double levelScore;

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
