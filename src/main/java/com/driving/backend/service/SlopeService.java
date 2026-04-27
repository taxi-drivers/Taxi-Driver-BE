package com.driving.backend.service;

import com.driving.backend.entity.GraphEdge;
import com.driving.backend.entity.GraphNode;
import com.driving.backend.repository.GraphEdgeRepository;
import com.driving.backend.repository.GraphNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 엣지별 경사도(slope) 계산 서비스.
 *
 * 공식: slope = (toNode.elevation − fromNode.elevation) / lengthM
 *  - 단위: 무차원 fraction (예: 0.05 = +5% 오르막)
 *  - 부호: 진행방향 기준 (양수=오르막, 음수=내리막)
 *
 * 사전 조건: ElevationService.populateMissingElevations() 가 선행되어 모든 노드에 elevation 값이 채워져 있어야 함.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlopeService {

    private final GraphEdgeRepository graphEdgeRepository;
    private final GraphNodeRepository graphNodeRepository;

    /**
     * 모든 엣지에 slope 값 계산해서 저장.
     *
     * @return 처리한 엣지 수
     */
    @Transactional
    public int computeAllSlopes() {
        long start = System.currentTimeMillis();

        // 노드 elevation 캐시 (조회 1번)
        Map<Long, Double> elevationCache = new HashMap<>();
        for (GraphNode node : graphNodeRepository.findAll()) {
            elevationCache.put(node.getNodeId(), node.getElevation());
        }
        log.info("[SlopeService] 노드 elevation 캐시 로드: {}건", elevationCache.size());

        List<GraphEdge> edges = graphEdgeRepository.findAll();
        int processed = 0;
        int skipped = 0;

        for (GraphEdge edge : edges) {
            Double fromElev = elevationCache.get(edge.getFromNode());
            Double toElev = elevationCache.get(edge.getToNode());

            if (fromElev == null || toElev == null || edge.getLengthM() == null || edge.getLengthM() <= 0) {
                skipped++;
                continue;
            }

            double slope = (toElev - fromElev) / edge.getLengthM();
            edge.setSlope(slope);
            processed++;
        }

        graphEdgeRepository.saveAll(edges);

        long elapsed = System.currentTimeMillis() - start;
        log.info("[SlopeService] 완료: 처리 {}건, skip {}건 ({}ms)", processed, skipped, elapsed);
        return processed;
    }
}
