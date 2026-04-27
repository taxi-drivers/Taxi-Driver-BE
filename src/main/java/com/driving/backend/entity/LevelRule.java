package com.driving.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "level_rule")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelRule {

    @Id
    @Column(name = "level_rule_id")
    private Long levelRuleId;

    @Column(nullable = false, unique = true, length = 30)
    private String version;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "w_accident_rate", nullable = false)
    private Double wAccidentRate;

    @Column(name = "w_road_shape", nullable = false)
    private Double wRoadShape;

    @Column(name = "w_road_scale", nullable = false)
    private Double wRoadScale;

    @Column(name = "w_intersection", nullable = false)
    private Double wIntersection;

    @Column(name = "w_traffic_volume", nullable = false)
    private Double wTrafficVolume;

    @Column(name = "level1_max", nullable = false)
    private Double level1Max;

    @Column(name = "level2_max", nullable = false)
    private Double level2Max;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
