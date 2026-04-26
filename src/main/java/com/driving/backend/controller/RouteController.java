package com.driving.backend.controller;

import com.driving.backend.dto.RouteResult;
import com.driving.backend.service.GraphService;
import com.driving.backend.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
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

    @PostMapping("/search")
    @Operation(summary = "경로 탐색", description = "출발지와 도착지를 기준으로 경로를 탐색합니다. vulnerabilities가 비어 있으면 로그인 사용자의 취약 특성을 사용합니다.")
    public ResponseEntity<?> searchRoute(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Map<String, Object> request
    ) {
        if (!graphService.isGraphReady()) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "그래프가 아직 준비되지 않았습니다."));
        }

        double startLat = ((Number) request.get("startLat")).doubleValue();
        double startLon = ((Number) request.get("startLon")).doubleValue();
        double endLat = ((Number) request.get("endLat")).doubleValue();
        double endLon = ((Number) request.get("endLon")).doubleValue();

        @SuppressWarnings("unchecked")
        List<String> vulnerabilities = (List<String>) request.get("vulnerabilities");
        if ((vulnerabilities == null || vulnerabilities.isEmpty()) && authorizationHeader != null) {
            vulnerabilities = userProfileService.getMyVulnerabilityCodes(authorizationHeader);
        }

        RouteResult result = graphService.findRoute(startLat, startLon, endLat, endLon, vulnerabilities);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    @Operation(summary = "그래프 준비 상태 조회", description = "OSM 그래프가 메모리에 로드되어 경로 탐색 가능한 상태인지 확인합니다.")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(Map.of("graphReady", graphService.isGraphReady()));
    }
}
