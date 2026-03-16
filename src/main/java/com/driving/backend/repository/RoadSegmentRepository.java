package com.driving.backend.repository;

import com.driving.backend.entity.RoadSegment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Provides database access methods through Spring Data JPA.
 */
public interface RoadSegmentRepository extends JpaRepository<RoadSegment, String> {
    List<RoadSegment> findByCenterLatBetweenAndCenterLonBetween(
            Double minLat,
            Double maxLat,
            Double minLon,
            Double maxLon
    );
}

