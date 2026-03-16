package com.driving.backend.controller;

import com.driving.backend.dto.RouteResult;
import com.driving.backend.service.GraphService;
import com.driving.backend.service.UserProfileService;
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
public class RouteController {

    private final GraphService graphService;
    private final UserProfileService userProfileService;

    @PostMapping("/search")
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
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(Map.of("graphReady", graphService.isGraphReady()));
    }
}
