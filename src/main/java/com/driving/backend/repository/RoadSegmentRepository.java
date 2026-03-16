package com.driving.backend.repository;

import com.driving.backend.entity.RoadSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoadSegmentRepository extends JpaRepository<RoadSegment, String> {

    /**
     * 지도 영역(bounds) 내 세그먼트 조회
     * API: GET /map/segments?minLat=..&minLon=..&maxLat=..&maxLon=..
     */
    @Query("SELECT r FROM RoadSegment r WHERE r.centerLat BETWEEN :minLat AND :maxLat AND r.centerLon BETWEEN :minLon AND :maxLon")
    List<RoadSegment> findByBounds(@Param("minLat") double minLat,
                                   @Param("minLon") double minLon,
                                   @Param("maxLat") double maxLat,
                                   @Param("maxLon") double maxLon);

    /**
     * 지도 영역 내 + 특정 레벨 필터
     */
    @Query("SELECT r FROM RoadSegment r WHERE r.centerLat BETWEEN :minLat AND :maxLat AND r.centerLon BETWEEN :minLon AND :maxLon AND r.level IN :levels")
    List<RoadSegment> findByBoundsAndLevels(@Param("minLat") double minLat,
                                            @Param("minLon") double minLon,
                                            @Param("maxLat") double maxLat,
                                            @Param("maxLon") double maxLon,
                                            @Param("levels") List<Integer> levels);

    List<RoadSegment> findByLevel(Integer level);

    List<RoadSegment> findByHighway(String highway);
}
