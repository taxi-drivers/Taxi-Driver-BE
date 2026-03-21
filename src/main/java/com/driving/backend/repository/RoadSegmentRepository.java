package com.driving.backend.repository;

import com.driving.backend.entity.RoadSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoadSegmentRepository extends JpaRepository<RoadSegment, String> {

    @Query("SELECT r FROM RoadSegment r WHERE r.centerLat BETWEEN :minLat AND :maxLat AND r.centerLon BETWEEN :minLon AND :maxLon")
    List<RoadSegment> findByBounds(@Param("minLat") double minLat,
                                   @Param("minLon") double minLon,
                                   @Param("maxLat") double maxLat,
                                   @Param("maxLon") double maxLon);

    @Query("SELECT r FROM RoadSegment r WHERE r.centerLat BETWEEN :minLat AND :maxLat AND r.centerLon BETWEEN :minLon AND :maxLon AND r.level IN :levels")
    List<RoadSegment> findByBoundsAndLevels(@Param("minLat") double minLat,
                                            @Param("minLon") double minLon,
                                            @Param("maxLat") double maxLat,
                                            @Param("maxLon") double maxLon,
                                            @Param("levels") List<Integer> levels);

    List<RoadSegment> findByCenterLatBetweenAndCenterLonBetween(
            Double minLat,
            Double maxLat,
            Double minLon,
            Double maxLon
    );

    List<RoadSegment> findByLevel(Integer level);

    List<RoadSegment> findByHighway(String highway);
}
