package com.driving.backend.repository;

import com.driving.backend.entity.RoadSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoadSegmentRepository extends JpaRepository<RoadSegment, String> {

    // bounds 기반 세그먼트 조회 (지도 뷰포트 내 세그먼트)
    @Query("SELECT r FROM RoadSegment r WHERE " +
           "r.centerLat BETWEEN :minLat AND :maxLat AND " +
           "r.centerLon BETWEEN :minLon AND :maxLon")
    List<RoadSegment> findByBounds(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon
    );

    // 레벨 필터링 (1: 쉬움, 2: 보통, 3: 어려움)
    List<RoadSegment> findByLevel(Integer level);
}
