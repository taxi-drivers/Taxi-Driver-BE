package com.driving.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "road_segments")
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
