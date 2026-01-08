package com.driving.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 타일 점수 Entity
 * - 평균 경사도, 혼잡도, 사고율 등
 */
@Entity
@Table(name = "tile_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TileScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "road_tile_id", nullable = false)
    private RoadTile roadTile;

    @Column
    private Double slopeAvg; // 평균 경사도

    @Column
    private Double congestion; // 혼잡도

    @Column
    private Double accidentRate; // 사고율

    @Column
    private Double laneWidth; // 차선 폭

    @Column
    private Integer intersectionCount; // 교차로 수
}
