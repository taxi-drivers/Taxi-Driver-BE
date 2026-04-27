package com.driving.backend.controller;

import com.driving.backend.service.ElevationService;
import com.driving.backend.service.SlopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 경사도 데이터 일회성 batch trigger 엔드포인트.
 *
 * 일반 사용자용이 아니라 데이터 준비용 admin 엔드포인트.
 *
 * 사용 순서:
 *   1. POST /api/admin/slope/elevations  ← 노드 8,939개 elevation 채우기 (~2분)
 *   2. POST /api/admin/slope/compute     ← 엣지 23,472개 slope 계산 (수초)
 *   3. POST /api/admin/slope/build-all   ← 위 두 단계를 순차 실행
 */
@RestController
@RequestMapping("/api/admin/slope")
@RequiredArgsConstructor
public class SlopeAdminController {

    private final ElevationService elevationService;
    private final SlopeService slopeService;

    /**
     * 1단계: OpenTopoData에서 노드 elevation 일괄 조회 후 DB 저장.
     * elevation IS NULL 인 노드만 처리 (재실행 안전).
     */
    @PostMapping("/elevations")
    public ResponseEntity<?> populateElevations() {
        int processed = elevationService.populateMissingElevations();
        return ResponseEntity.ok(Map.of(
                "step", "elevations",
                "processed", processed
        ));
    }

    /**
     * 2단계: 모든 엣지 slope 계산 후 DB 저장.
     * (toNode.elevation - fromNode.elevation) / lengthM
     */
    @PostMapping("/compute")
    public ResponseEntity<?> computeSlopes() {
        int processed = slopeService.computeAllSlopes();
        return ResponseEntity.ok(Map.of(
                "step", "compute",
                "processed", processed
        ));
    }

    /**
     * 1+2 통합 실행. 신규 환경 셋업 시 한 번만 호출하면 됨.
     */
    @PostMapping("/build-all")
    public ResponseEntity<?> buildAll() {
        int elevations = elevationService.populateMissingElevations();
        int slopes = slopeService.computeAllSlopes();
        return ResponseEntity.ok(Map.of(
                "elevationsProcessed", elevations,
                "slopesProcessed", slopes
        ));
    }
}
