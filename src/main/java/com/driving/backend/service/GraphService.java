package com.driving.backend.service;

import com.driving.backend.dto.RouteResult;
import com.driving.backend.entity.GraphEdge;
import com.driving.backend.entity.GraphNode;
import com.driving.backend.repository.GraphEdgeRepository;
import com.driving.backend.repository.GraphNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * OSM 도로 그래프 기반 경로 탐색 서비스.
 *
 * - 서버 시작 시 DB에서 노드/엣지 로드 → JGraphT 인메모리 그래프 구축
 * - A* 알고리즘으로 경로 탐색 (사용자 취약특성 기반 동적 가중치)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GraphService {

    private final GraphNodeRepository graphNodeRepository;
    private final GraphEdgeRepository graphEdgeRepository;

    // 인메모리 그래프
    private SimpleDirectedWeightedGraph<Long, WeightedRoadEdge> graph;

    // 노드 좌표 캐시 (경로 탐색 시 heuristic용)
    private Map<Long, double[]> nodeCoords;

    // 엣지 메타데이터 캐시 (경로 결과에 난이도 정보 포함용)
    private Map<WeightedRoadEdge, GraphEdge> edgeMetadata;

    /**
     * 그래프 구축. DataLoader에서 CSV 임포트 완료 후 호출.
     */
    public void buildGraph() {
        long start = System.currentTimeMillis();

        List<GraphNode> nodes = graphNodeRepository.findAll();
        List<GraphEdge> edges = graphEdgeRepository.findAll();

        if (nodes.isEmpty() || edges.isEmpty()) {
            log.warn("[GraphService] 노드 또는 엣지 데이터가 없습니다. 그래프 미구축.");
            return;
        }

        graph = new SimpleDirectedWeightedGraph<>(WeightedRoadEdge.class);
        nodeCoords = new HashMap<>(nodes.size());
        edgeMetadata = new HashMap<>(edges.size());

        // 노드 추가
        for (GraphNode node : nodes) {
            graph.addVertex(node.getNodeId());
            nodeCoords.put(node.getNodeId(), new double[]{node.getLat(), node.getLon()});
        }

        // 엣지 추가 (기본 가중치: 거리 기반)
        int added = 0;
        for (GraphEdge edge : edges) {
            Long from = edge.getFromNode();
            Long to = edge.getToNode();

            if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
                continue;
            }
            if (from.equals(to)) {
                continue;
            }

            // 같은 방향의 중복 엣지 방지
            if (graph.containsEdge(from, to)) {
                continue;
            }

            WeightedRoadEdge e = graph.addEdge(from, to);
            if (e != null) {
                // 기본 가중치: 거리 (미터)
                graph.setEdgeWeight(e, edge.getLengthM());
                edgeMetadata.put(e, edge);
                added++;
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("[GraphService] 그래프 구축 완료: 노드 {}개, 엣지 {}개 ({}ms)",
                graph.vertexSet().size(), added, elapsed);
    }

    /**
     * 좌표로 가장 가까운 노드 찾기.
     */
    public Long findNearestNode(double lat, double lon) {
        Long nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Map.Entry<Long, double[]> entry : nodeCoords.entrySet()) {
            double[] coords = entry.getValue();
            double dist = haversineM(lat, lon, coords[0], coords[1]);
            if (dist < minDist) {
                minDist = dist;
                nearest = entry.getKey();
            }
        }

        return nearest;
    }

    /**
     * 경로 탐색 (기본: 거리 기반).
     */
    public RouteResult findRoute(double startLat, double startLon,
                                  double endLat, double endLon) {
        return findRoute(startLat, startLon, endLat, endLon, null);
    }

    /**
     * 경로 탐색 (난이도 가중치 적용).
     *
     * @param vulnerabilityTypeCodes 사용자 취약특성 코드 리스트 (null이면 기본 경로)
     */
    public RouteResult findRoute(double startLat, double startLon,
                                  double endLat, double endLon,
                                  List<String> vulnerabilityTypeCodes) {
        return findRoute(startLat, startLon, endLat, endLon, vulnerabilityTypeCodes, null);
    }

    public RouteResult findRoute(double startLat, double startLon,
                                  double endLat, double endLon,
                                  List<String> vulnerabilityTypeCodes,
                                  Integer skillLevel) {
        if (graph == null) {
            throw new IllegalStateException("그래프가 구축되지 않았습니다.");
        }

        Long startNode = findNearestNode(startLat, startLon);
        Long endNode = findNearestNode(endLat, endLon);

        if (startNode == null || endNode == null) {
            throw new IllegalArgumentException("출발/도착 좌표 근처에 노드를 찾을 수 없습니다.");
        }

        // 난이도 가중치 적용
        boolean useDifficulty = (vulnerabilityTypeCodes != null && !vulnerabilityTypeCodes.isEmpty())
                || skillLevel != null;
        if (useDifficulty) {
            applyDifficultyWeights(vulnerabilityTypeCodes, skillLevel);
        } else {
            applyDistanceOnlyWeights();
        }

        // A* 경로 탐색
        AStarShortestPath<Long, WeightedRoadEdge> astar = new AStarShortestPath<>(
                graph, (source, target) -> {
                    double[] s = nodeCoords.get(source);
                    double[] t = nodeCoords.get(target);
                    if (s == null || t == null) return 0.0;
                    return haversineM(s[0], s[1], t[0], t[1]);
                }
        );

        GraphPath<Long, WeightedRoadEdge> path;
        try {
            path = astar.getPath(startNode, endNode);
        } catch (Exception e) {
            throw new IllegalStateException("경로를 찾을 수 없습니다: " + e.getMessage());
        }

        if (path == null) {
            throw new IllegalStateException("출발지에서 도착지까지 경로가 존재하지 않습니다.");
        }

        return buildRouteResult(path);
    }

    /**
     * 거리만 사용하는 가중치 (최단 거리 경로).
     * service 도로(주차장 진입로/골목)는 통과 경로로 부적절하므로 페널티 적용.
     * 경사도(slope)도 기본 가중치에 포함 — 가파른 도로는 모든 운전자에게 부담.
     */
    private void applyDistanceOnlyWeights() {
        for (Map.Entry<WeightedRoadEdge, GraphEdge> entry : edgeMetadata.entrySet()) {
            GraphEdge edge = entry.getValue();
            double cost = edge.getLengthM()
                    * getHighwayPenalty(edge.getHighway())
                    * getSlopeMultiplier(edge.getSlope());
            graph.setEdgeWeight(entry.getKey(), cost);
        }
    }

    /**
     * highway 타입별 cost 페널티.
     * service: 주차장 진입로/건물 골목 → 통과 경로로 부적절. 다른 도로 있으면 회피.
     */
    private double getHighwayPenalty(String highway) {
        if ("service".equals(highway)) return 3.0;
        return 1.0;
    }

    /**
     * 경사도(slope) cost 배수.
     *
     * SRTM 30m DEM 기반 slope는 도시 지역(빌딩 밀집/짧은 엣지)에서
     * 노이즈로 ±100% 같은 비현실적 값이 나올 수 있으므로 ±15%로 clamp.
     * (실제 도로 최대 경사 ~12% 기준)
     *
     * 부호 무관: 오르막/내리막 둘 다 초보 운전자에게 부담 (가속/제동 모두 어려움).
     *
     * 결과: slope=0%→1.0x, 5%→1.15x, 10%→1.30x, 15%↑→1.45x
     */
    private double getSlopeMultiplier(Double slope) {
        if (slope == null) return 1.0;
        final double SLOPE_CLAMP = 0.15;
        final double SLOPE_COEFFICIENT = 3.0;
        double clamped = Math.min(Math.abs(slope), SLOPE_CLAMP);
        return 1.0 + SLOPE_COEFFICIENT * clamped;
    }

    /**
     * 난이도 기반 가중치 적용.
     *
     * cost = distance × (1 + α × normalizedDifficulty + β × vulnerabilityPenalty)
     *
     * - α: 기본 난이도 가중치 (0.5)
     * - β: 취약특성 매칭 시 추가 페널티 (1.0)
     */
    private void applyDifficultyWeights(List<String> vulnerabilityCodes, Integer skillLevel) {
        final double ALPHA = 0.5;  // 기본 난이도 영향
        final double BETA = 1.0;   // 취약특성 추가 페널티

        double skillSensitivity = calculateSkillSensitivity(skillLevel);
        double alpha = 0.25 + (0.75 * skillSensitivity);
        double beta = 0.8 + (0.7 * skillSensitivity);

        Set<String> vulnSet = vulnerabilityCodes == null
                ? Set.of()
                : new HashSet<>(vulnerabilityCodes);

        for (Map.Entry<WeightedRoadEdge, GraphEdge> entry : edgeMetadata.entrySet()) {
            GraphEdge edge = entry.getValue();
            double distance = edge.getLengthM();
            double difficulty = edge.getTotalScore() != null ? edge.getTotalScore() : 35.0;

            // 난이도 0~100 → 0~1 정규화
            double normalizedDiff = difficulty / 100.0;

            // 취약특성 페널티 계산
            double vulnPenalty = 0.0;
            String highway = edge.getHighway();

            if (vulnSet.contains("AVOID_HIGHWAY") &&
                    (highway != null && (highway.equals("trunk") || highway.equals("trunk_link")))) {
                vulnPenalty += 1.0;
            }
            if (vulnSet.contains("PREFER_WIDE_ROAD") &&
                    edge.getRoadScaleScore() != null && edge.getRoadScaleScore() >= 70) {
                vulnPenalty += 0.8;
            }
            if (vulnSet.contains("AVOID_COMPLEX_INTERSECTION") &&
                    edge.getIntersectionScore() != null && edge.getIntersectionScore() >= 50) {
                vulnPenalty += 0.8;
            }
            if (vulnSet.contains("AVOID_HIGH_TRAFFIC") &&
                    edge.getTrafficVolumeScore() != null && edge.getTrafficVolumeScore() >= 60) {
                vulnPenalty += 0.8;
            }
            if (vulnSet.contains("AVOID_ACCIDENT_PRONE") &&
                    edge.getAccidentRateScore() != null && edge.getAccidentRateScore() >= 50) {
                vulnPenalty += 1.0;
            }

            double cost = distance * (1.0 + alpha * normalizedDiff + beta * vulnPenalty);
            cost *= getHighwayPenalty(highway);
            cost *= getSlopeMultiplier(edge.getSlope());
            graph.setEdgeWeight(entry.getKey(), cost);
        }
    }

    /**
     * 경로 결과 조립.
     */
    private RouteResult buildRouteResult(GraphPath<Long, WeightedRoadEdge> path) {
        List<RouteResult.RouteSegment> segments = new ArrayList<>();
        double totalDistance = 0;
        double totalDifficulty = 0;
        List<double[]> allCoordinates = new ArrayList<>();

        for (WeightedRoadEdge edge : path.getEdgeList()) {
            GraphEdge meta = edgeMetadata.get(edge);
            if (meta == null) continue;

            totalDistance += meta.getLengthM();
            double diff = meta.getTotalScore() != null ? meta.getTotalScore() : 35.0;
            totalDifficulty += diff * meta.getLengthM();

            segments.add(RouteResult.RouteSegment.builder()
                    .edgeId(meta.getEdgeId())
                    .name(meta.getName())
                    .highway(meta.getHighway())
                    .lengthM(meta.getLengthM())
                    .difficulty(diff)
                    .coordinatesJson(meta.getCoordinatesJson())
                    .accidentRateScore(meta.getAccidentRateScore())
                    .roadShapeScore(meta.getRoadShapeScore())
                    .roadScaleScore(meta.getRoadScaleScore())
                    .intersectionScore(meta.getIntersectionScore())
                    .trafficVolumeScore(meta.getTrafficVolumeScore())
                    .slope(meta.getSlope())
                    .build());
        }

        double avgDifficulty = totalDistance > 0 ? totalDifficulty / totalDistance : 0;
        // 예상 시간 (평균 30km/h 가정, 난이도 높으면 감속)
        double avgSpeedKmh = 30.0 * (1.0 - avgDifficulty / 200.0);
        int estimatedMinutes = (int) Math.ceil((totalDistance / 1000.0) / avgSpeedKmh * 60);

        return RouteResult.builder()
                .totalDistanceM(Math.round(totalDistance))
                .estimatedMinutes(estimatedMinutes)
                .avgDifficulty(Math.round(avgDifficulty * 10) / 10.0)
                .segments(segments)
                .build();
    }

    /**
     * Haversine 거리 계산 (미터).
     */
    private double haversineM(double lat1, double lon1, double lat2, double lon2) {
        double R = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double calculateSkillSensitivity(Integer skillLevel) {
        if (skillLevel == null) {
            return 0.5;
        }

        int clamped = Math.max(0, Math.min(100, skillLevel));
        return (100.0 - clamped) / 100.0;
    }

    public boolean isGraphReady() {
        return graph != null && !graph.vertexSet().isEmpty();
    }

    /**
     * JGraphT용 커스텀 엣지 클래스.
     */
    public static class WeightedRoadEdge extends DefaultWeightedEdge {
    }
}
