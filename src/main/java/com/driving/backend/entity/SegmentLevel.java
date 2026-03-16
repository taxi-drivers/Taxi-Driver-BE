package com.driving.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "segment_level")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentLevel {

    @EmbeddedId
    private SegmentLevelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("segmentId")
    @JoinColumn(name = "segment_id")
    private RoadSegment roadSegment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("levelRuleId")
    @JoinColumn(name = "level_rule_id")
    private LevelRule levelRule;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "level_score")
    private Double levelScore;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;
}
