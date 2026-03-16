package com.driving.backend.controller;

import com.driving.backend.dto.*;
import com.driving.backend.service.RoadSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 도로 세그먼트 조회 API 컨트롤러
 *
 * 담당: 박승종 (PSJ)
 * 기준 Entity: RoadSegment (SegmentScore + SegmentLevel 통합 구조, [JY] 2주차 통합)
 *
 * 엔드포인트 목록:
 *   GET /api/segments                  - 지도 bounds 기반 세그먼트 목록 조회
 *   GET /api/segments/{id}             - 세그먼트 상세 조회
 *   GET /api/segments/{id}/tooltip     - 지도 hover 툴팁용 요약 정보
 *   GET /api/segments/{id}/difficulty  - 난이도 (level, totalScore, levelText)
 *   GET /api/segments/{id}/score-detail - 5개 세부 점수 구성요소
 */
@RestController
@RequestMapping("/api/segments")
@RequiredArgsConstructor
public class RoadSegmentController {

    private final RoadSegmentService roadSegmentService;

    /**
     * 지도 뷰포트(bounds) 내 도로 세그먼트 목록 조회
     *
     * 카카오맵에서 현재 화면에 보이는 영역의 세그먼트를 반환.
     * center_lat / center_lon 기준으로 bounds 필터링.
     *
     * @param minLat 남쪽 위도 (예: 37.4910)
     * @param maxLat 북쪽 위도 (예: 37.5160)
     * @param minLon 서쪽 경도 (예: 127.0200)
     * @param maxLon 동쪽 경도 (예: 127.0650)
     * @return 해당 bounds 내 세그먼트 요약 목록 (id, 좌표, level, totalScore)
     */
    @GetMapping
    public ResponseEntity<List<SegmentSummaryResponse>> getSegmentsByBounds(
            @RequestParam Double minLat,
            @RequestParam Double maxLat,
            @RequestParam Double minLon,
            @RequestParam Double maxLon
    ) {
        return ResponseEntity.ok(roadSegmentService.getSegmentsByBounds(minLat, maxLat, minLon, maxLon));
    }

    /**
     * 도로 세그먼트 상세 조회
     *
     * 특정 세그먼트의 전체 정보 반환.
     * 지도에서 도로 클릭 시 상세 패널에 표시.
     *
     * @param id 세그먼트 ID (예: "SEG_000001")
     * @return 전체 필드 (좌표, 난이도, 설명, polyline 등)
     */
    @GetMapping("/{id}")
    public ResponseEntity<SegmentDetailResponse> getSegmentById(@PathVariable String id) {
        return ResponseEntity.ok(roadSegmentService.getSegmentById(id));
    }

    /**
     * 지도 hover 툴팁용 요약 정보 조회
     *
     * 마우스 hover 시 말풍선에 표시되는 간단한 정보.
     * 응답 필드: segmentId, name, level, levelText, totalScore, explanation
     *
     * @param id 세그먼트 ID
     * @return 툴팁 표시용 요약 정보
     */
    @GetMapping("/{id}/tooltip")
    public ResponseEntity<SegmentTooltipResponse> getTooltip(@PathVariable String id) {
        return ResponseEntity.ok(roadSegmentService.getTooltip(id));
    }

    /**
     * 난이도 조회
     *
     * 세그먼트의 난이도 레벨과 점수만 반환 (지도 색상 렌더링용).
     * level: 1(쉬움, ≤31.0), 2(보통, ≤41.8), 3(어려움, >41.8)
     *
     * @param id 세그먼트 ID
     * @return level(1~3), levelText, totalScore(0~100)
     */
    @GetMapping("/{id}/difficulty")
    public ResponseEntity<SegmentDifficultyResponse> getDifficulty(@PathVariable String id) {
        return ResponseEntity.ok(roadSegmentService.getDifficulty(id));
    }

    /**
     * 난이도 구성요소(세부 점수) 조회
     *
     * 상세 패널의 "점수 산출 근거" 섹션에 표시.
     * 난이도 공식: 0.25×사고율 + 0.20×도로형태 + 0.15×도로규모 + 0.15×교차로 + 0.25×교통량
     *
     * @param id 세그먼트 ID
     * @return totalScore + 5개 세부 점수 (accidentRate, roadShape, roadScale, intersection, trafficVolume)
     */
    @GetMapping("/{id}/score-detail")
    public ResponseEntity<SegmentScoreDetailResponse> getScoreDetail(@PathVariable String id) {
        return ResponseEntity.ok(roadSegmentService.getScoreDetail(id));
    }
}
