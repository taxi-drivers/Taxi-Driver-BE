package com.driving.backend.service;

import com.driving.backend.dto.map.AreaSummaryRequest;
import com.driving.backend.dto.map.AreaSummaryResponse;
import com.driving.backend.dto.map.AreaSummaryResponse.DifficultyDistribution;
import com.driving.backend.dto.map.AreaSummaryResponse.WarningIndicator;
import com.driving.backend.dto.map.MapEdgeItem;
import com.driving.backend.entity.GraphEdge;
import com.driving.backend.repository.GraphEdgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapModeService {

    private final GraphEdgeRepository graphEdgeRepository;

    // 난이도 cut-off (level_rule과 동일)
    private static final double LEVEL1_MAX = 30.7;
    private static final double LEVEL2_MAX = 41.5;

    // 위험 임계값 (RoadSegment 매핑 규칙과 동일)
    private static final double WARN_INTERSECTION = 50.0;
    private static final double WARN_ROAD_SCALE = 70.0;
    private static final double WARN_TRAFFIC = 60.0;
    private static final double WARN_ACCIDENT = 50.0;
    private static final double STEEP_SLOPE = 0.06;

    /**
     * 줌 레벨별 표시할 highway 타입.
     * 낮은 줌에서는 큰 도로만 → 폴리라인 수 대폭 감소.
     */
    private static final List<String> HIGHWAYS_LOW_ZOOM = List.of(
            "trunk", "trunk_link", "primary", "primary_link"
    );
    private static final List<String> HIGHWAYS_MID_ZOOM = List.of(
            "trunk", "trunk_link", "primary", "primary_link",
            "secondary", "secondary_link", "tertiary", "tertiary_link"
    );

    /**
     * 지도 모드 폴리라인 렌더링용 경량 엣지 목록.
     * zoom < 15면 큰 도로(primary/trunk)만, zoom 15면 +secondary/tertiary, zoom 16+면 전체.
     */
    public List<MapEdgeItem> getEdgesByBounds(double minLat, double maxLat, double minLon, double maxLon, Integer zoom) {
        List<com.driving.backend.entity.GraphEdge> edges;
        int z = zoom == null ? 16 : zoom;
        if (z <= 14) {
            edges = graphEdgeRepository.findByBoundsAndHighwayIn(minLat, maxLat, minLon, maxLon, HIGHWAYS_LOW_ZOOM);
        } else if (z == 15) {
            edges = graphEdgeRepository.findByBoundsAndHighwayIn(minLat, maxLat, minLon, maxLon, HIGHWAYS_MID_ZOOM);
        } else {
            edges = graphEdgeRepository.findByBounds(minLat, maxLat, minLon, maxLon);
        }
        return edges.stream()
                .map(e -> MapEdgeItem.builder()
                        .edgeId(e.getEdgeId())
                        .difficulty(e.getTotalScore() != null ? e.getTotalScore() : 0.0)
                        .coordinatesJson(simplifyCoordinatesJson(e.getCoordinatesJson()))
                        .build())
                .toList();
    }

    /**
     * coordinates_json은 [[lon, lat], [lon, lat], ...] 형태.
     * 정밀도 6→5자리로 줄임 (약 0.1m → 1m, 시각상 차이 없음).
     */
    private String simplifyCoordinatesJson(String json) {
        if (json == null || json.length() < 4) return json;
        // 빠른 정규식 없이 단순 문자 수준 round: 정밀도 6자리 → 5자리
        // [127.012345, 37.501234] → [127.01235, 37.50123]
        // 안전한 방법: 파싱 후 round 하지만 비용. 단순 패턴이면 문자열에서 소수점 5자리로 자르기.
        StringBuilder sb = new StringBuilder(json.length());
        int i = 0;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '.') {
                sb.append(c);
                int decimals = 0;
                int j = i + 1;
                while (j < json.length() && Character.isDigit(json.charAt(j))) {
                    if (decimals < 5) {
                        sb.append(json.charAt(j));
                        decimals++;
                    }
                    j++;
                }
                i = j;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * 영역 통계 분석.
     */
    public AreaSummaryResponse summarizeArea(AreaSummaryRequest req) {
        List<GraphEdge> edges = graphEdgeRepository.findByBounds(
                req.getMinLat(), req.getMaxLat(), req.getMinLon(), req.getMaxLon());

        if (edges.isEmpty()) {
            return AreaSummaryResponse.builder()
                    .edgeCount(0)
                    .avgDifficulty(0)
                    .safetyScore(0)
                    .difficultyDistribution(DifficultyDistribution.builder().build())
                    .warningIndicators(List.of())
                    .summary("선택한 영역에 분석 가능한 도로 데이터가 없습니다.")
                    .build();
        }

        int total = edges.size();
        int low = 0, mid = 0, high = 0;
        double sumDifficulty = 0;
        int complexInter = 0, narrowRoad = 0, serviceRoad = 0, highTraffic = 0, accidentProne = 0, steepSlope = 0;

        for (GraphEdge e : edges) {
            double d = e.getTotalScore() != null ? e.getTotalScore() : 0.0;
            sumDifficulty += d;
            if (d <= LEVEL1_MAX) low++;
            else if (d <= LEVEL2_MAX) mid++;
            else high++;

            if (e.getIntersectionScore() != null && e.getIntersectionScore() >= WARN_INTERSECTION) complexInter++;
            if (e.getRoadScaleScore() != null && e.getRoadScaleScore() >= WARN_ROAD_SCALE) narrowRoad++;
            if ("service".equals(e.getHighway())) serviceRoad++;
            if (e.getTrafficVolumeScore() != null && e.getTrafficVolumeScore() >= WARN_TRAFFIC) highTraffic++;
            if (e.getAccidentRateScore() != null && e.getAccidentRateScore() >= WARN_ACCIDENT) accidentProne++;
            if (e.getSlope() != null && Math.abs(e.getSlope()) >= STEEP_SLOPE) steepSlope++;
        }

        double avg = sumDifficulty / total;
        // safetyScore: 난이도 평균이 낮을수록 높음. 100점 만점 → 10점 척도로 역변환
        double safety = Math.max(0, Math.min(10, (100 - avg) / 10.0));

        DifficultyDistribution dist = DifficultyDistribution.builder()
                .low(low).mid(mid).high(high)
                .lowPct(round1(low * 100.0 / total))
                .midPct(round1(mid * 100.0 / total))
                .highPct(round1(high * 100.0 / total))
                .build();

        List<WarningIndicator> warnings = new ArrayList<>();
        if (complexInter > 0) warnings.add(warning("complex_intersection", "복잡한 교차로", complexInter, "신호등·횡단보도가 많아 주의 필요"));
        if (narrowRoad > 0) warnings.add(warning("narrow_road", "좁은 도로", narrowRoad, "도로 폭이 좁아 차선 변경 어려움"));
        if (serviceRoad > 0) warnings.add(warning("service_road", "주차장·골목", serviceRoad, "통과 경로로 부적절"));
        if (highTraffic > 0) warnings.add(warning("high_traffic", "교통량 많음", highTraffic, "혼잡 시간대 주의"));
        if (accidentProne > 0) warnings.add(warning("accident_prone", "사고다발구간", accidentProne, "반경 300m 내 사고 다발"));
        if (steepSlope > 0) warnings.add(warning("steep_slope", "가파른 경사", steepSlope, "오르막·내리막 6% 이상"));

        return AreaSummaryResponse.builder()
                .edgeCount(total)
                .avgDifficulty(round1(avg))
                .safetyScore(round1(safety))
                .difficultyDistribution(dist)
                .warningIndicators(warnings)
                .summary(buildTemplateSummary(total, avg, dist, warnings))
                .build();
    }

    private WarningIndicator warning(String type, String label, int count, String description) {
        return WarningIndicator.builder()
                .type(type).label(label).count(count).description(description)
                .build();
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    /**
     * LLM 미연결 시 사용할 템플릿 기반 자연어 요약.
     */
    private String buildTemplateSummary(int total, double avg,
                                        DifficultyDistribution dist,
                                        List<WarningIndicator> warnings) {
        StringBuilder sb = new StringBuilder();
        sb.append("선택 영역에는 ").append(total).append("개의 도로가 포함되어 있습니다. ");
        sb.append("평균 난이도는 ").append(round1(avg)).append("점");

        String level;
        if (avg <= LEVEL1_MAX) level = "쉬움";
        else if (avg <= LEVEL2_MAX) level = "보통";
        else level = "어려움";
        sb.append("(").append(level).append(")이며, ");

        sb.append("어려움 등급 도로가 ").append(round1(dist.getHighPct())).append("%를 차지합니다. ");

        if (warnings.isEmpty()) {
            sb.append("뚜렷한 위험 요소는 발견되지 않아 비교적 무난한 구간입니다.");
        } else {
            sb.append("주요 위험 요소: ");
            for (int i = 0; i < warnings.size(); i++) {
                WarningIndicator w = warnings.get(i);
                sb.append(w.getLabel()).append(" ").append(w.getCount()).append("곳");
                if (i < warnings.size() - 1) sb.append(", ");
            }
            sb.append(".");
        }

        return sb.toString();
    }
}
