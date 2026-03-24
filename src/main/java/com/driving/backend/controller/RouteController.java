package com.driving.backend.controller;

import com.driving.backend.dto.RouteResult;
import com.driving.backend.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final GraphService graphService;

    /**
     * 경로 탐색 API.
     *
     * POST /api/routes/search
     * {
     *   "startLat": 37.4979,
     *   "startLon": 127.0276,
     *   "endLat": 37.5172,
     *   "endLon": 127.0473,
     *   "vulnerabilities": ["AVOID_HIGHWAY", "AVOID_COMPLEX_INTERSECTION"]  // optional
     * }
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchRoute(@RequestBody Map<String, Object> request) {
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

        RouteResult result = graphService.findRoute(
                startLat, startLon, endLat, endLon, vulnerabilities);

        return ResponseEntity.ok(result);
    }

    /**
     * 그래프 상태 확인 API.
     *
     * GET /api/routes/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(Map.of(
                "graphReady", graphService.isGraphReady()
        ));
    }
}
