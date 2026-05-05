package com.driving.backend.controller;

import com.driving.backend.dto.map.AreaSummaryRequest;
import com.driving.backend.dto.map.AreaSummaryResponse;
import com.driving.backend.dto.map.MapEdgeItem;
import com.driving.backend.service.MapModeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 지도 모드 API: 화면 영역 내 도로 폴리라인 + 영역 통계 분석.
 */
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@Tag(name = "Map Mode", description = "지도 모드: 도로 오버레이 + 영역 분석")
public class MapModeController {

    private final MapModeService mapModeService;

    /**
     * 지도 화면(bounds) 안의 엣지를 폴리라인 렌더링용 경량 형태로 반환.
     */
    @GetMapping("/edges")
    @Operation(summary = "bounds 안의 엣지 조회", description = "지도 모드에서 화면 영역 내 도로를 가져옴 (zoom으로 highway 필터링)")
    public ResponseEntity<List<MapEdgeItem>> getEdges(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon,
            @RequestParam(required = false) Integer zoom) {
        return ResponseEntity.ok(mapModeService.getEdgesByBounds(minLat, maxLat, minLon, maxLon, zoom));
    }

    /**
     * 사용자가 드래그한 영역의 통계 분석.
     */
    @PostMapping("/area-summary")
    @Operation(summary = "영역 도로 분석", description = "선택 영역의 난이도 분포·평균·위험 요소 카운트 + 자연어 요약")
    public ResponseEntity<AreaSummaryResponse> summarizeArea(@RequestBody AreaSummaryRequest req) {
        return ResponseEntity.ok(mapModeService.summarizeArea(req));
    }
}
