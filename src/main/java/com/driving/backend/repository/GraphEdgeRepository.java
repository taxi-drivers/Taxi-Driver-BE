package com.driving.backend.repository;

import com.driving.backend.entity.GraphEdge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraphEdgeRepository extends JpaRepository<GraphEdge, String> {
}
