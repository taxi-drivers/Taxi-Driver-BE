package com.driving.backend.repository;

import com.driving.backend.entity.GraphNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraphNodeRepository extends JpaRepository<GraphNode, Long> {
}
