package com.driving.backend.controller;

import com.driving.backend.dto.RouteSearchRequest;
import com.driving.backend.dto.RouteResult;
import com.driving.backend.service.GraphService;
import com.driving.backend.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Route", description = "OSM 기반 경로 탐색 API")
public class RouteController {

    private final GraphService graphService;
    private final UserProfileService userProfileService;

    @Operation(summary = "경로 탐색", description = "취약특성 기반 가중치를 적용해 OSM 경로를 탐색합니다. vulnerabilities를 비우고 인증 헤더를 보내면 저장된 설문 결과를 자동 사용합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탐색 성공",
                    content = @Content(schema = @Schema(implementation = RouteResult.class))),
            @ApiResponse(responseCode = "503", description = "그래프가 아직 준비되지 않음")
    })
    @PostMapping("/search")
    public ResponseEntity<?> searchRoute(
            @Parameter(description = "Bearer Access Token. vulnerabilities를 직접 보내지 않을 때 사용자 설문 결과 자동 반영", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody RouteSearchRequest request
    ) {
        if (!graphService.isGraphReady()) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "그래프가 아직 준비되지 않았습니다."));
        }

        double startLat = request.startLat();
        double startLon = request.startLon();
        double endLat = request.endLat();
        double endLon = request.endLon();

        List<String> vulnerabilities = request.vulnerabilities();
        if ((vulnerabilities == null || vulnerabilities.isEmpty()) && authorizationHeader != null) {
            vulnerabilities = userProfileService.getMyVulnerabilityCodes(authorizationHeader);
        }

        RouteResult result = graphService.findRoute(startLat, startLon, endLat, endLon, vulnerabilities);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "그래프 준비 상태 조회", description = "OSM 그래프가 메모리에 로드되어 경로 탐색이 가능한 상태인지 확인합니다.")
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(Map.of("graphReady", graphService.isGraphReady()));
    }
}
