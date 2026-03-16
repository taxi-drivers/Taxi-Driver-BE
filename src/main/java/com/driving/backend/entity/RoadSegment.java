package com.driving.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Defines a domain model mapped to a database table.
 */
@Entity
@Table(name = "road_segments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadSegment {

    @Id
    @Column(name = "segment_id")
    private String segmentId;

    @Column(name = "name")
    private String name;

    @Column(name = "highway")
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

    @Column(name = "coordinates_json", nullable = false, columnDefinition = "json")
    private String coordinatesJson;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "level_text")
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

    @Column(name = "explanation", length = 300)
    private String explanation;

    @Lob
    @Column(name = "detail_description")
    private String detailDescription;

    @Column(name = "evidence_json", columnDefinition = "json")
    private String evidenceJson;

    @Column(name = "level_rule_id", nullable = false)
    private Long levelRuleId;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "level_score")
    private Double levelScore;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

