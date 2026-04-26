package com.driving.backend.controller;

import com.driving.backend.dto.map.MapInitResponse;
import com.driving.backend.dto.map.MapSegmentDetailResponse;
import com.driving.backend.dto.map.MapSegmentsResponse;
import com.driving.backend.dto.map.SegmentVulnerabilityDetailsResponse;
import com.driving.backend.dto.map.VulnerableSegmentsResponse;
import com.driving.backend.service.MapInitService;
import com.driving.backend.service.MapSegmentService;
import com.driving.backend.service.MapVulnerableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles HTTP requests and maps them to service-layer operations.
 */
@RestController
@RequestMapping("/map")
@Tag(name = "Map", description = "지도 초기화와 세그먼트 조회 API")
public class MapSegmentController {

    private final MapSegmentService mapSegmentService;
    private final MapInitService mapInitService;
    private final MapVulnerableService mapVulnerableService;

    public MapSegmentController(
            MapSegmentService mapSegmentService,
            MapInitService mapInitService,
            MapVulnerableService mapVulnerableService
    ) {
        this.mapSegmentService = mapSegmentService;
        this.mapInitService = mapInitService;
        this.mapVulnerableService = mapVulnerableService;
    }

    @GetMapping("/segments")
    @Operation(summary = "지도 세그먼트 조회", description = "지도 bounds 안에 포함되는 세그먼트를 조회합니다.")
    public ResponseEntity<MapSegmentsResponse> getSegments(
            @RequestParam("minLat") String minLat,
            @RequestParam("minLon") String minLon,
            @RequestParam("maxLat") String maxLat,
            @RequestParam("maxLon") String maxLon,
            @RequestParam(value = "levels", required = false) String levels,
            @RequestParam(value = "simplified", required = false) String simplified,
            @RequestParam(value = "computedAfter", required = false) String computedAfter
    ) {
        return ResponseEntity.ok(mapSegmentService.getSegments(
                minLat,
                minLon,
                maxLat,
                maxLon,
                levels,
                simplified,
                computedAfter
        ));
    }

    @GetMapping("/segments/vulnerable")
    @Operation(summary = "취약 세그먼트 조회", description = "사용자 취약 특성과 지도 bounds를 기준으로 취약 세그먼트를 조회합니다.")
    public ResponseEntity<VulnerableSegmentsResponse> getVulnerableSegments(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("minLat") String minLat,
            @RequestParam("minLon") String minLon,
            @RequestParam("maxLat") String maxLat,
            @RequestParam("maxLon") String maxLon,
            @RequestParam(value = "minSeverity", required = false) String minSeverity,
            @RequestParam(value = "returnMode", required = false) String returnMode,
            @RequestParam(value = "levels", required = false) String levels
    ) {
        return ResponseEntity.ok(mapVulnerableService.getVulnerableSegments(
                authorizationHeader,
                minLat,
                minLon,
                maxLat,
                maxLon,
                minSeverity,
                returnMode,
                levels
        ));
    }

    @GetMapping("/segments/{segment_id}/detail")
    @Operation(summary = "세그먼트 상세 조회", description = "특정 세그먼트의 상세 정보를 조회합니다.")
    public ResponseEntity<MapSegmentDetailResponse> getSegmentDetail(@PathVariable("segment_id") String segmentId) {
        return ResponseEntity.ok(mapSegmentService.getSegmentDetail(segmentId));
    }

    @GetMapping("/segments/{segment_id}/vulnerabilities")
    @Operation(summary = "세그먼트 취약요소 조회", description = "세그먼트별 취약 요소 상세를 조회합니다.")
    public ResponseEntity<SegmentVulnerabilityDetailsResponse> getSegmentVulnerabilities(
            @PathVariable("segment_id") String segmentId
    ) {
        return ResponseEntity.ok(mapSegmentService.getSegmentVulnerabilities(segmentId));
    }

    @GetMapping("/init")
    @Operation(summary = "지도 초기화", description = "지도 렌더링에 필요한 사용자 정보와 세그먼트 초기 데이터를 함께 반환합니다.")
    public ResponseEntity<MapInitResponse> initMap(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("minLat") String minLat,
            @RequestParam("minLon") String minLon,
            @RequestParam("maxLat") String maxLat,
            @RequestParam("maxLon") String maxLon,
            @RequestParam(value = "includeSegments", required = false) String includeSegments,
            @RequestParam(value = "levels", required = false) String levels
    ) {
        return ResponseEntity.ok(mapInitService.init(
                authorizationHeader,
                minLat,
                minLon,
                maxLat,
                maxLon,
                includeSegments,
                levels
        ));
    }
}

