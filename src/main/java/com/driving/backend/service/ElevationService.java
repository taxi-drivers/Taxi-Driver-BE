package com.driving.backend.service;

import com.driving.backend.entity.GraphNode;
import com.driving.backend.repository.GraphNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

/**
 * 노드별 고도(elevation) 일괄 조회 서비스.
 *
 * - 데이터 소스: OpenTopoData 공개 API (https://www.opentopodata.org)
 * - 데이터셋: srtm30m (전 세계 SRTM 30m 격자, 한국 영토 포함)
 * - Rate limit: 1 request/sec, 최대 100 location/요청
 *
 * 일회성 batch 용도. elevation이 NULL인 노드만 처리하므로 중복 호출 안전.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ElevationService {

    private static final String API_URL = "https://api.opentopodata.org/v1/srtm30m";
    private static final int BATCH_SIZE = 100;
    private static final long REQUEST_INTERVAL_MS = 1100L;

    private final GraphNodeRepository graphNodeRepository;

    private final RestTemplate restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(30))
            .build();

    /**
     * elevation이 NULL인 모든 노드의 고도를 OpenTopoData에서 조회 후 저장.
     *
     * @return 처리한 노드 수
     */
    @Transactional
    public int populateMissingElevations() {
        List<GraphNode> targets = graphNodeRepository.findAll().stream()
                .filter(n -> n.getElevation() == null)
                .toList();

        if (targets.isEmpty()) {
            log.info("[ElevationService] 처리 대상 노드 없음. 모든 노드에 elevation 존재.");
            return 0;
        }

        int totalNodes = targets.size();
        int totalBatches = (int) Math.ceil(totalNodes / (double) BATCH_SIZE);
        long start = System.currentTimeMillis();

        log.info("[ElevationService] 시작: 대상 노드 {}건, 배치 {}회 (≈{}분 소요 예상)",
                totalNodes, totalBatches, Math.round(totalBatches * REQUEST_INTERVAL_MS / 60_000.0));

        int processed = 0;
        int failed = 0;

        for (int b = 0; b < totalBatches; b++) {
            int from = b * BATCH_SIZE;
            int to = Math.min(from + BATCH_SIZE, totalNodes);
            List<GraphNode> chunk = targets.subList(from, to);

            try {
                List<Double> elevations = fetchElevations(chunk);

                for (int i = 0; i < chunk.size(); i++) {
                    Double elev = i < elevations.size() ? elevations.get(i) : null;
                    if (elev != null) {
                        chunk.get(i).setElevation(elev);
                        processed++;
                    } else {
                        failed++;
                    }
                }
                graphNodeRepository.saveAll(chunk);

                if ((b + 1) % 10 == 0 || b == totalBatches - 1) {
                    log.info("[ElevationService] 진행: {}/{} 배치 ({}건 처리, {}건 실패)",
                            b + 1, totalBatches, processed, failed);
                }
            } catch (Exception e) {
                log.error("[ElevationService] 배치 {} 실패: {}", b + 1, e.getMessage());
                failed += chunk.size();
            }

            if (b < totalBatches - 1) {
                sleep(REQUEST_INTERVAL_MS);
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("[ElevationService] 완료: 처리 {}건, 실패 {}건 ({}초)",
                processed, failed, elapsed / 1000);
        return processed;
    }

    /**
     * OpenTopoData API 호출 (locations=lat,lon|lat,lon|...).
     * 응답: { results: [{ elevation: 35.2, location: {lat, lng}, dataset: "srtm30m" }, ...] }
     */
    @SuppressWarnings("unchecked")
    private List<Double> fetchElevations(List<GraphNode> chunk) {
        StringBuilder locations = new StringBuilder();
        for (int i = 0; i < chunk.size(); i++) {
            if (i > 0) locations.append("|");
            GraphNode n = chunk.get(i);
            locations.append(n.getLat()).append(",").append(n.getLon());
        }

        String url = API_URL + "?locations=" + locations;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("results")) {
            throw new IllegalStateException("OpenTopoData 응답에 results 필드 없음: " + response);
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        List<Double> elevations = new ArrayList<>(results.size());
        for (Map<String, Object> r : results) {
            Object e = r.get("elevation");
            elevations.add(e instanceof Number ? ((Number) e).doubleValue() : null);
        }
        return elevations;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
