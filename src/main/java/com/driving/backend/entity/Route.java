package com.driving.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 경로 Entity
 * - 출발/도착지, 거리, 평균 난이도, 폴리라인 등
 */
@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String startLocation; // 출발지

    @Column(nullable = false)
    private String endLocation; // 도착지

    @Column
    private Double startLatitude;

    @Column
    private Double startLongitude;

    @Column
    private Double endLatitude;

    @Column
    private Double endLongitude;

    @Column
    private Double distance; // 거리 (km)

    @Column
    private Double avgDifficulty; // 평균 난이도

    @Lob
    @Column(columnDefinition = "TEXT")
    private String polyline; // 폴리라인 좌표 데이터

    @Column
    private Integer estimatedTime; // 예상 소요 시간 (분)

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
