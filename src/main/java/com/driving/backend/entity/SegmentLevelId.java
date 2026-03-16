package com.driving.backend.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SegmentLevelId implements Serializable {

    private String segmentId;
    private Long levelRuleId;
}
