package com.driving.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "graph_edges", indexes = {
        @Index(name = "idx_from_node", columnList = "from_node"),
        @Index(name = "idx_to_node", columnList = "to_node")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraphEdge {

    @Id
    @Column(name = "edge_id", length = 50)
    private String edgeId;

    @Column(name = "from_node", nullable = false)
    private Long fromNode;

    @Column(name = "to_node", nullable = false)
    private Long toNode;

    @Column(name = "edge_key")
    private Integer edgeKey;

    @Column(length = 120)
    private String name;

    @Column(length = 30)
    private String highway;

    @Column(name = "length_m", nullable = false)
    private Double lengthM;

    @Column
    private Boolean oneway;

    @Column(name = "center_lat")
    private Double centerLat;

    @Column(name = "center_lon")
    private Double centerLon;

    @Column(name = "coordinates_json", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String coordinatesJson;

    // ── 난이도 점수 (기존 세그먼트에서 매핑) ──

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "accident_rate_score")
    private Double accidentRateScore;

    @Column(name = "road_shape_score")
    private Double roadShapeScore;

    @Column(name = "road_scale_score")
    private Double roadScaleScore;

    @Column(name = "intersection_score")
    private Double intersectionScore;

    @Column(name = "traffic_volume_score")
    private Double trafficVolumeScore;

    @Column(name = "matched_segment_id", length = 50)
    private String matchedSegmentId;

    @Column(name = "match_distance_m")
    private Double matchDistanceM;

    /**
     * 경사도 (signed gradient, fraction 단위).
     * = (toNode.elevation − fromNode.elevation) / lengthM
     * 양수: 진행방향 오르막, 음수: 내리막. 예: 0.05 = +5% 경사.
     */
    @Column
    private Double slope;

    public void setSlope(Double slope) {
        this.slope = slope;
    }
}
