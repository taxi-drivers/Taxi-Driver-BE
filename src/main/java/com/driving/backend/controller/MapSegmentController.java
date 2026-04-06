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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/map")
@Tag(name = "Map", description = "지도 초기화, 지도 세그먼트, 취약지도 API")
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

    @Operation(summary = "지도 세그먼트 조회", description = "현재 보이는 지도 bounds 안의 도로 세그먼트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = MapSegmentsResponse.class)))
    @GetMapping("/segments")
    public ResponseEntity<MapSegmentsResponse> getSegments(
            @Parameter(description = "최소 위도", example = "37.49") @RequestParam("minLat") String minLat,
            @Parameter(description = "최소 경도", example = "127.02") @RequestParam("minLon") String minLon,
            @Parameter(description = "최대 위도", example = "37.52") @RequestParam("maxLat") String maxLat,
            @Parameter(description = "최대 경도", example = "127.06") @RequestParam("maxLon") String maxLon,
            @Parameter(description = "레벨 필터. 쉼표 구분", example = "1,2,3")
            @RequestParam(value = "levels", required = false) String levels,
            @Parameter(description = "좌표 단순화 여부", example = "false")
            @RequestParam(value = "simplified", required = false) String simplified,
            @Parameter(description = "이 시각 이후 계산된 세그먼트만 조회", example = "2026-03-21T22:00:00")
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

    @Operation(summary = "사용자 취약 세그먼트 조회", description = "로그인 사용자의 취약특성에 맞는 위험 구간을 지도 bounds 내에서 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/segments/vulnerable")
    public ResponseEntity<VulnerableSegmentsResponse> getVulnerableSegments(
            @Parameter(description = "Bearer Access Token", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "최소 위도", example = "37.49") @RequestParam("minLat") String minLat,
            @Parameter(description = "최소 경도", example = "127.02") @RequestParam("minLon") String minLon,
            @Parameter(description = "최대 위도", example = "37.52") @RequestParam("maxLat") String maxLat,
            @Parameter(description = "최대 경도", example = "127.06") @RequestParam("maxLon") String maxLon,
            @Parameter(description = "최소 취약 강도", example = "0.5") @RequestParam(value = "minSeverity", required = false) String minSeverity,
            @Parameter(description = "SEGMENT면 coordinates 포함", example = "SEGMENT") @RequestParam(value = "returnMode", required = false) String returnMode,
            @Parameter(description = "레벨 필터. 쉼표 구분", example = "2,3") @RequestParam(value = "levels", required = false) String levels
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

    @Operation(summary = "지도 상세 패널 조회", description = "특정 세그먼트의 상세 패널 정보를 조회합니다.")
    @GetMapping("/segments/{segment_id}/detail")
    public ResponseEntity<MapSegmentDetailResponse> getSegmentDetail(@PathVariable("segment_id") String segmentId) {
        return ResponseEntity.ok(mapSegmentService.getSegmentDetail(segmentId));
    }

    @Operation(summary = "도로별 취약특성 상세 조회", description = "특정 세그먼트에 매핑된 취약특성 목록을 조회합니다.")
    @GetMapping("/segments/{segment_id}/vulnerabilities")
    public ResponseEntity<SegmentVulnerabilityDetailsResponse> getSegmentVulnerabilities(
            @PathVariable("segment_id") String segmentId
    ) {
        return ResponseEntity.ok(mapSegmentService.getSegmentVulnerabilities(segmentId));
    }

    @Operation(summary = "메인 지도 초기화", description = "메인 페이지 진입 시 사용자 요약, 색상 규칙, 취약특성 메타, 지도 세그먼트를 한 번에 내려줍니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/init")
    public ResponseEntity<MapInitResponse> initMap(
            @Parameter(description = "Bearer Access Token", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "최소 위도", example = "37.49") @RequestParam("minLat") String minLat,
            @Parameter(description = "최소 경도", example = "127.02") @RequestParam("minLon") String minLon,
            @Parameter(description = "최대 위도", example = "37.52") @RequestParam("maxLat") String maxLat,
            @Parameter(description = "최대 경도", example = "127.06") @RequestParam("maxLon") String maxLon,
            @Parameter(description = "세그먼트도 같이 포함할지 여부", example = "true")
            @RequestParam(value = "includeSegments", required = false) String includeSegments,
            @Parameter(description = "레벨 필터. 쉼표 구분", example = "1,2,3")
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

