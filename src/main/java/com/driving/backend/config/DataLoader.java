package com.driving.backend.config;

import com.driving.backend.entity.*;
import com.driving.backend.repository.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 서버 시작 시 CSV 데이터를 MySQL에 자동 임포트하는 DataLoader.
 *
 * - CSV 파일 위치: src/main/resources/data/
 * - FK 의존성 순서대로 임포트
 * - 이미 데이터가 있으면 skip (중복 방지)
 * - "이름없음" → null 변환
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final VulnerabilityTypeRepository vulnerabilityTypeRepository;
    private final LevelRuleRepository levelRuleRepository;
    private final RoadSegmentRepository roadSegmentRepository;
    private final SegmentVulnerabilityMapRepository segmentVulnerabilityMapRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int BATCH_SIZE = 500;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("========== DataLoader 시작 ==========");

        loadVulnerabilityTypes();
        loadLevelRules();
        loadRoadSegments();
        loadSegmentVulnerabilityMaps();
        loadMockUser();

        log.info("========== DataLoader 완료 ==========");
    }

    // ── 1. vulnerability_type (5건) ──

    private void loadVulnerabilityTypes() throws Exception {
        if (vulnerabilityTypeRepository.count() > 0) {
            log.info("[SKIP] vulnerability_type: 이미 {}건 존재", vulnerabilityTypeRepository.count());
            return;
        }

        List<String[]> rows = readCsv("data/vulnerability_type.csv");
        List<VulnerabilityType> entities = new ArrayList<>();

        for (String[] row : rows) {
            entities.add(VulnerabilityType.builder()
                    .vulnerabilityTypeId(parseInt(row[0]))
                    .code(row[1])
                    .name(row[2])
                    .description(row[3])
                    .iconKey(row[4])
                    .build());
        }

        vulnerabilityTypeRepository.saveAll(entities);
        log.info("[OK] vulnerability_type: {}건 임포트", entities.size());
    }

    // ── 2. level_rule (1건) ──

    private void loadLevelRules() throws Exception {
        if (levelRuleRepository.count() > 0) {
            log.info("[SKIP] level_rule: 이미 {}건 존재", levelRuleRepository.count());
            return;
        }

        List<String[]> rows = readCsv("data/level_rule.csv");
        List<LevelRule> entities = new ArrayList<>();

        for (String[] row : rows) {
            entities.add(LevelRule.builder()
                    .levelRuleId(parseLong(row[0]))
                    .version(row[1])
                    .name(row[2])
                    .wAccidentRate(parseDouble(row[3]))
                    .wRoadShape(parseDouble(row[4]))
                    .wRoadScale(parseDouble(row[5]))
                    .wIntersection(parseDouble(row[6]))
                    .wTrafficVolume(parseDouble(row[7]))
                    .level1Max(parseDouble(row[8]))
                    .level2Max(parseDouble(row[9]))
                    .isActive(parseBoolean(row[10]))
                    .createdAt(parseDateTime(row[11]))
                    .build());
        }

        levelRuleRepository.saveAll(entities);
        log.info("[OK] level_rule: {}건 임포트", entities.size());
    }

    // ── 3. road_segments (7,200건) = road_segments.csv + segment_score.csv + segment_level.csv merge ──

    private void loadRoadSegments() throws Exception {
        if (roadSegmentRepository.count() > 0) {
            log.info("[SKIP] road_segments: 이미 {}건 존재", roadSegmentRepository.count());
            return;
        }

        // 3-1. road_segments.csv 읽기 (지리 정보)
        List<String[]> segRows = readCsv("data/road_segments.csv");
        Map<String, String[]> segMap = new LinkedHashMap<>();
        for (String[] row : segRows) {
            segMap.put(row[0], row); // key: segment_id
        }

        // 3-2. segment_score.csv 읽기 (점수)
        List<String[]> scoreRows = readCsv("data/segment_score.csv");
        Map<String, String[]> scoreMap = new HashMap<>();
        for (String[] row : scoreRows) {
            scoreMap.put(row[0], row);
        }

        // 3-3. segment_level.csv 읽기 (레벨)
        List<String[]> levelRows = readCsv("data/segment_level.csv");
        Map<String, String[]> levelMap = new HashMap<>();
        for (String[] row : levelRows) {
            levelMap.put(row[0], row);
        }

        // 3-4. LevelRule 참조 가져오기
        LevelRule activeLevelRule = levelRuleRepository.findByIsActiveTrue()
                .orElseThrow(() -> new RuntimeException("활성 LevelRule이 없습니다. level_rule 데이터를 먼저 임포트하세요."));

        // 3-5. merge & save (batch)
        List<RoadSegment> batch = new ArrayList<>();
        int total = 0;

        for (Map.Entry<String, String[]> entry : segMap.entrySet()) {
            String segmentId = entry.getKey();
            String[] seg = entry.getValue();
            String[] score = scoreMap.get(segmentId);
            String[] level = levelMap.get(segmentId);

            if (score == null || level == null) {
                log.warn("[WARN] segment_id={} 에 score 또는 level 데이터 누락, skip", segmentId);
                continue;
            }

            // seg: segment_id,name,highway,start_lat,start_lon,end_lat,end_lon,center_lat,center_lon,num_points,coordinates_json,created_at,updated_at
            // score: segment_id,total_score,level_text,accident_rate_score,road_shape_score,road_scale_score,intersection_score,traffic_volume_score,explanation,detail_description,detail_source,detail_source_ref,detail_updated_at,computed_at
            // level: segment_id,level_rule_id,level,level_score,computed_at

            String name = seg[1];
            if ("이름없음".equals(name)) {
                name = null;
            }

            RoadSegment entity = RoadSegment.builder()
                    .segmentId(segmentId)
                    .name(name)
                    .highway(seg[2])
                    .startLat(parseDouble(seg[3]))
                    .startLon(parseDouble(seg[4]))
                    .endLat(parseDouble(seg[5]))
                    .endLon(parseDouble(seg[6]))
                    .centerLat(parseDouble(seg[7]))
                    .centerLon(parseDouble(seg[8]))
                    .numPoints(parseInt(seg[9]))
                    .coordinatesJson(seg[10])
                    // segment_score 필드
                    .totalScore(parseDouble(score[1]))
                    .levelText(score[2])
                    .accidentRateScore(parseDouble(score[3]))
                    .roadShapeScore(parseDouble(score[4]))
                    .roadScaleScore(parseDouble(score[5]))
                    .intersectionScore(parseDouble(score[6]))
                    .trafficVolumeScore(parseDouble(score[7]))
                    .explanation(score[8])
                    .detailDescription(score[9])
                    // segment_level 필드
                    .levelRule(activeLevelRule)
                    .level(parseInt(level[2]))
                    .levelScore(parseDouble(level[3]))
                    .computedAt(parseDateTime(level[4]))
                    // 시각
                    .createdAt(parseDateTime(seg[11]))
                    .updatedAt(parseDateTimeOrNull(seg[12]))
                    .build();

            batch.add(entity);

            if (batch.size() >= BATCH_SIZE) {
                roadSegmentRepository.saveAll(batch);
                total += batch.size();
                batch.clear();
                log.info("[진행] road_segments: {}건 저장 완료", total);
            }
        }

        if (!batch.isEmpty()) {
            roadSegmentRepository.saveAll(batch);
            total += batch.size();
        }

        log.info("[OK] road_segments: 총 {}건 임포트", total);
    }

    // ── 4. segment_vulnerability_map (9,018건) ──

    private void loadSegmentVulnerabilityMaps() throws Exception {
        if (segmentVulnerabilityMapRepository.count() > 0) {
            log.info("[SKIP] segment_vulnerability_map: 이미 {}건 존재", segmentVulnerabilityMapRepository.count());
            return;
        }

        List<String[]> rows = readCsv("data/segment_vulnerability_map.csv");

        // 참조 엔티티 캐시
        Map<String, RoadSegment> segCache = new HashMap<>();
        Map<Integer, VulnerabilityType> vtCache = new HashMap<>();
        vulnerabilityTypeRepository.findAll().forEach(vt -> vtCache.put(vt.getVulnerabilityTypeId(), vt));

        List<SegmentVulnerabilityMap> batch = new ArrayList<>();
        int total = 0;

        for (String[] row : rows) {
            // row: segment_id, vulnerability_type_id, severity, note, source, updated_at
            String segmentId = row[0];
            Integer vtId = parseInt(row[1]);

            RoadSegment seg = segCache.computeIfAbsent(segmentId,
                    id -> roadSegmentRepository.findById(id).orElse(null));
            VulnerabilityType vt = vtCache.get(vtId);

            if (seg == null || vt == null) {
                continue;
            }

            SegmentVulnerabilityMap entity = SegmentVulnerabilityMap.builder()
                    .id(new SegmentVulnerabilityMapId(segmentId, vtId))
                    .roadSegment(seg)
                    .vulnerabilityType(vt)
                    .severity(parseDouble(row[2]))
                    .note(row[3])
                    .source(row[4])
                    .updatedAt(parseDateTimeOrNull(row[5]))
                    .build();

            batch.add(entity);

            if (batch.size() >= BATCH_SIZE) {
                segmentVulnerabilityMapRepository.saveAll(batch);
                total += batch.size();
                batch.clear();
                log.info("[진행] segment_vulnerability_map: {}건 저장 완료", total);
            }
        }

        if (!batch.isEmpty()) {
            segmentVulnerabilityMapRepository.saveAll(batch);
            total += batch.size();
        }

        log.info("[OK] segment_vulnerability_map: 총 {}건 임포트", total);
    }

    // ── 5. mock user (1건) ──

    private void loadMockUser() {
        if (userRepository.count() > 0) {
            log.info("[SKIP] users: 이미 {}건 존재", userRepository.count());
            return;
        }

        User mockUser = User.builder()
                .email("test@test.com")
                .nickname("테스트유저")
                .build();
        userRepository.save(mockUser);

        UserProfile profile = UserProfile.builder()
                .user(mockUser)
                .skillLevel(50)
                .build();
        userProfileRepository.save(profile);

        log.info("[OK] mock user 생성 (id={}, email=test@test.com, skillLevel=50)", mockUser.getUserId());
    }

    // ── CSV 파싱 유틸 ──

    private List<String[]> readCsv(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                .build()) {

            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) return allRows;

            // BOM 제거 (첫 번째 행의 첫 번째 값)
            String firstVal = allRows.get(0)[0];
            if (firstVal.startsWith("\uFEFF")) {
                allRows.get(0)[0] = firstVal.substring(1);
            }

            // 헤더(첫 행) 제거
            allRows.remove(0);

            // 각 셀 trim
            for (String[] row : allRows) {
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].trim();
                }
            }

            return allRows;
        }
    }

    private Double parseDouble(String val) {
        if (val == null || val.isEmpty()) return null;
        return Double.parseDouble(val);
    }

    private Integer parseInt(String val) {
        if (val == null || val.isEmpty()) return null;
        return (int) Double.parseDouble(val); // CSV에서 "70.0" 같은 형태 대비
    }

    private Long parseLong(String val) {
        if (val == null || val.isEmpty()) return null;
        return (long) Double.parseDouble(val);
    }

    private Boolean parseBoolean(String val) {
        if (val == null || val.isEmpty()) return null;
        return "True".equalsIgnoreCase(val) || "true".equalsIgnoreCase(val);
    }

    private LocalDateTime parseDateTime(String val) {
        if (val == null || val.isEmpty()) return null;
        return LocalDateTime.parse(val, DT_FMT);
    }

    private LocalDateTime parseDateTimeOrNull(String val) {
        if (val == null || val.isEmpty()) return null;
        try {
            return LocalDateTime.parse(val, DT_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
