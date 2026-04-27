package com.driving.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "graph_nodes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraphNode {

    @Id
    @Column(name = "node_id")
    private Long nodeId;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lon;

    @Column
    private Double elevation;

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }
}
