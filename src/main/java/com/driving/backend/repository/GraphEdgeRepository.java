package com.driving.backend.repository;

import com.driving.backend.entity.GraphEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GraphEdgeRepository extends JpaRepository<GraphEdge, String> {

    /**
     * 지도 영역(bounds) 안의 엣지 조회. 지도 모드에서 화면에 보이는 엣지만 가져옴.
     */
    @Query("SELECT e FROM GraphEdge e WHERE e.centerLat BETWEEN :minLat AND :maxLat " +
           "AND e.centerLon BETWEEN :minLon AND :maxLon")
    List<GraphEdge> findByBounds(@Param("minLat") double minLat,
                                  @Param("maxLat") double maxLat,
                                  @Param("minLon") double minLon,
                                  @Param("maxLon") double maxLon);

    /**
     * highway 타입 필터 + bounds 조회. 줌 레벨이 낮을 때 큰 도로만 가져오기 위함.
     */
    @Query("SELECT e FROM GraphEdge e WHERE e.centerLat BETWEEN :minLat AND :maxLat " +
           "AND e.centerLon BETWEEN :minLon AND :maxLon AND e.highway IN :highways")
    List<GraphEdge> findByBoundsAndHighwayIn(@Param("minLat") double minLat,
                                              @Param("maxLat") double maxLat,
                                              @Param("minLon") double minLon,
                                              @Param("maxLon") double maxLon,
                                              @Param("highways") List<String> highways);
}
