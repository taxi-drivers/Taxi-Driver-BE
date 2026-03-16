package com.driving.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "level_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_rule_id")
    private Long levelRuleId;

    @Column(name = "version", nullable = false, unique = true)
    private String version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "w_accident_rate", nullable = false)
    private Double weightAccidentRate;

    @Column(name = "w_road_shape", nullable = false)
    private Double weightRoadShape;

    @Column(name = "w_road_scale", nullable = false)
    private Double weightRoadScale;

    @Column(name = "w_intersection", nullable = false)
    private Double weightIntersection;

    @Column(name = "w_traffic_volume", nullable = false)
    private Double weightTrafficVolume;

    @Column(name = "level1_max", nullable = false)
    private Double level1Max;

    @Column(name = "level2_max", nullable = false)
    private Double level2Max;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

