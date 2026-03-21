package com.driving.backend.config;

import com.driving.backend.entity.LevelRule;
import com.driving.backend.entity.RoadSegment;
import com.driving.backend.entity.SegmentVulnerabilityMap;
import com.driving.backend.entity.SegmentVulnerabilityMapId;
import com.driving.backend.entity.User;
import com.driving.backend.entity.UserVulnerabilityMap;
import com.driving.backend.entity.VulnerabilityType;
import com.driving.backend.repository.LevelRuleRepository;
import com.driving.backend.repository.RoadSegmentRepository;
import com.driving.backend.repository.SegmentVulnerabilityMapRepository;
import com.driving.backend.repository.UserRepository;
import com.driving.backend.repository.UserVulnerabilityMapRepository;
import com.driving.backend.repository.VulnerabilityTypeRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int BATCH_SIZE = 500;

    private final VulnerabilityTypeRepository vulnerabilityTypeRepository;
    private final LevelRuleRepository levelRuleRepository;
    private final RoadSegmentRepository roadSegmentRepository;
    private final SegmentVulnerabilityMapRepository segmentVulnerabilityMapRepository;
    private final UserRepository userRepository;
    private final UserVulnerabilityMapRepository userVulnerabilityMapRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("========== DataLoader start ==========");

        loadVulnerabilityTypes();
        loadLevelRules();
        loadRoadSegments();
        loadSegmentVulnerabilityMaps();
        loadMockUser();

        log.info("========== DataLoader complete ==========");
    }

    private void loadVulnerabilityTypes() throws Exception {
        if (vulnerabilityTypeRepository.count() > 0) {
            log.info("[SKIP] vulnerability_type already exists: {}", vulnerabilityTypeRepository.count());
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
        log.info("[OK] vulnerability_type imported: {}", entities.size());
    }

    private void loadLevelRules() throws Exception {
        if (levelRuleRepository.count() > 0) {
            log.info("[SKIP] level_rule already exists: {}", levelRuleRepository.count());
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
        log.info("[OK] level_rule imported: {}", entities.size());
    }

    private void loadRoadSegments() throws Exception {
        if (roadSegmentRepository.count() > 0) {
            log.info("[SKIP] road_segments already exists: {}", roadSegmentRepository.count());
            return;
        }

        List<String[]> segmentRows = readCsv("data/road_segments.csv");
        Map<String, String[]> segmentMap = new LinkedHashMap<>();
        for (String[] row : segmentRows) {
            segmentMap.put(row[0], row);
        }

        List<String[]> scoreRows = readCsv("data/segment_score.csv");
        Map<String, String[]> scoreMap = new HashMap<>();
        for (String[] row : scoreRows) {
            scoreMap.put(row[0], row);
        }

        List<String[]> levelRows = readCsv("data/segment_level.csv");
        Map<String, String[]> levelMap = new HashMap<>();
        for (String[] row : levelRows) {
            levelMap.put(row[0], row);
        }

        LevelRule activeLevelRule = levelRuleRepository.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException("Active level rule is required before loading segments"));

        List<RoadSegment> batch = new ArrayList<>();
        int total = 0;

        for (Map.Entry<String, String[]> entry : segmentMap.entrySet()) {
            String segmentId = entry.getKey();
            String[] segment = entry.getValue();
            String[] score = scoreMap.get(segmentId);
            String[] level = levelMap.get(segmentId);

            if (score == null || level == null) {
                log.warn("[WARN] Missing score or level row for segment_id={}", segmentId);
                continue;
            }

            String name = segment[1];
            if ("이름없음".equals(name)) {
                name = null;
            }

            batch.add(RoadSegment.builder()
                    .segmentId(segmentId)
                    .name(name)
                    .highway(segment[2])
                    .startLat(parseDouble(segment[3]))
                    .startLon(parseDouble(segment[4]))
                    .endLat(parseDouble(segment[5]))
                    .endLon(parseDouble(segment[6]))
                    .centerLat(parseDouble(segment[7]))
                    .centerLon(parseDouble(segment[8]))
                    .numPoints(parseInt(segment[9]))
                    .coordinatesJson(segment[10])
                    .totalScore(parseDouble(score[1]))
                    .levelText(score[2])
                    .accidentRateScore(parseDouble(score[3]))
                    .roadShapeScore(parseDouble(score[4]))
                    .roadScaleScore(parseDouble(score[5]))
                    .intersectionScore(parseDouble(score[6]))
                    .trafficVolumeScore(parseDouble(score[7]))
                    .explanation(score[8])
                    .detailDescription(score[9])
                    .levelRule(activeLevelRule)
                    .level(parseInt(level[2]))
                    .levelScore(parseDouble(level[3]))
                    .computedAt(parseDateTime(level[4]))
                    .createdAt(parseDateTime(segment[11]))
                    .updatedAt(parseDateTimeOrNull(segment[12]))
                    .build());

            if (batch.size() >= BATCH_SIZE) {
                roadSegmentRepository.saveAll(batch);
                total += batch.size();
                batch.clear();
                log.info("[RUNNING] road_segments imported: {}", total);
            }
        }

        if (!batch.isEmpty()) {
            roadSegmentRepository.saveAll(batch);
            total += batch.size();
        }

        log.info("[OK] road_segments imported: {}", total);
    }

    private void loadSegmentVulnerabilityMaps() throws Exception {
        if (segmentVulnerabilityMapRepository.count() > 0) {
            log.info("[SKIP] segment_vulnerability_map already exists: {}", segmentVulnerabilityMapRepository.count());
            return;
        }

        List<String[]> rows = readCsv("data/segment_vulnerability_map.csv");

        Map<String, RoadSegment> segmentCache = new HashMap<>();
        Map<Integer, VulnerabilityType> vulnerabilityTypeCache = new HashMap<>();
        vulnerabilityTypeRepository.findAll().forEach(type -> vulnerabilityTypeCache.put(type.getVulnerabilityTypeId(), type));

        List<SegmentVulnerabilityMap> batch = new ArrayList<>();
        int total = 0;

        for (String[] row : rows) {
            String segmentId = row[0];
            Integer vulnerabilityTypeId = parseInt(row[1]);

            RoadSegment segment = segmentCache.computeIfAbsent(segmentId,
                    id -> roadSegmentRepository.findById(id).orElse(null));
            VulnerabilityType vulnerabilityType = vulnerabilityTypeCache.get(vulnerabilityTypeId);

            if (segment == null || vulnerabilityType == null) {
                continue;
            }

            batch.add(SegmentVulnerabilityMap.builder()
                    .id(new SegmentVulnerabilityMapId(segmentId, vulnerabilityTypeId))
                    .roadSegment(segment)
                    .vulnerabilityType(vulnerabilityType)
                    .severity(parseDouble(row[2]))
                    .note(row[3])
                    .source(row[4])
                    .updatedAt(parseDateTimeOrNull(row[5]))
                    .build());

            if (batch.size() >= BATCH_SIZE) {
                segmentVulnerabilityMapRepository.saveAll(batch);
                total += batch.size();
                batch.clear();
                log.info("[RUNNING] segment_vulnerability_map imported: {}", total);
            }
        }

        if (!batch.isEmpty()) {
            segmentVulnerabilityMapRepository.saveAll(batch);
            total += batch.size();
        }

        log.info("[OK] segment_vulnerability_map imported: {}", total);
    }

    private void loadMockUser() {
        if (userRepository.count() > 0) {
            log.info("[SKIP] users already exists: {}", userRepository.count());
            return;
        }

        User mockUser = User.builder()
                .email("test@test.com")
                .nickname("Mock User")
                .skillLevel(50)
                .vulnerabilityTypeId(1)
                .build();
        userRepository.save(mockUser);

        userVulnerabilityMapRepository.save(UserVulnerabilityMap.builder()
                .userId(mockUser.getUserId())
                .vulnerabilityTypeId(1)
                .build());

        log.info("[OK] mock user created (id={}, email=test@test.com, skillLevel=50)", mockUser.getUserId());
    }

    private List<String[]> readCsv(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                .build()) {

            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) {
                return allRows;
            }

            String firstValue = allRows.get(0)[0];
            if (firstValue.startsWith("\uFEFF")) {
                allRows.get(0)[0] = firstValue.substring(1);
            }

            allRows.remove(0);

            for (String[] row : allRows) {
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].trim();
                }
            }

            return allRows;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Double.parseDouble(value);
    }

    private Integer parseInt(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return (int) Double.parseDouble(value);
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return (long) Double.parseDouble(value);
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return "True".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(value, DT_FMT);
    }

    private LocalDateTime parseDateTimeOrNull(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DT_FMT);
        } catch (Exception ex) {
            return null;
        }
    }
}
