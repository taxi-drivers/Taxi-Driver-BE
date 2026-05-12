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

    // safe 모드:
    //   - 보편적으로 위험한 2개 (고속도로 / 사고다발) → 강하게 회피
    //   - 나머지 (좁은 도로 / 복잡 교차로 / 급경사) → 약하게 비선호
    // 객관적 난이도를 낮추되 과한 우회는 방지하는 절충.
    private static final Map<String, Double> SAFE_MODE_STRENGTHS = Map.of(
            "AVOID_HIGHWAY", 1.0,
            "AVOID_ACCIDENT_PRONE", 1.0,
            "PREFER_WIDE_ROAD", 0.4,
            "AVOID_COMPLEX_INTERSECTION", 0.4,
            "AVOID_STEEP_SLOPE", 0.4
    );

    @PostMapping("/search")
    @Operation(summary = "경로 탐색", description = "mode=personal(설문 강도 기반) / safe(고속도로+사고다발 회피) / fast(거리만)")
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

        String mode = request.get("mode") instanceof String modeStr ? modeStr : "personal";

        Map<String, Double> vulnStrengths;
        Integer skillLevel = null;

        switch (mode) {
            case "fast" -> vulnStrengths = Map.of();
            case "safe" -> vulnStrengths = SAFE_MODE_STRENGTHS;
            default -> {
                // personal: 사용자의 최신 설문 응답에서 추출한 strength
                if (authorizationHeader == null) {
                    vulnStrengths = Map.of();
                } else {
                    try {
                        UserProfileService.DrivingPreference preference =
                                userProfileService.getMyDrivingPreference(authorizationHeader);
                        vulnStrengths = preference.strengths();
                        skillLevel = preference.skillLevel();
                    } catch (Exception e) {
                        vulnStrengths = Map.of();
                    }
                }
            }
        }

        RouteResult result = graphService.findRoute(startLat, startLon, endLat, endLon, vulnStrengths, skillLevel);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    @Operation(summary = "그래프 준비 상태 조회", description = "OSM 그래프가 메모리에 로드되어 경로 탐색 가능한 상태인지 확인합니다.")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(Map.of("graphReady", graphService.isGraphReady()));
    }
}
