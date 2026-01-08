package com.driving.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 도로 타일 Entity
 * - 타일ID, 위경도, 기본 난이도 점수 등
 */
@Entity
@Table(name = "road_tiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadTile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tileId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Integer difficulty; // 0~100점

    @Column(length = 500)
    private String explanation; // 난이도 설명

    @OneToOne(mappedBy = "roadTile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TileScore tileScore;
}
