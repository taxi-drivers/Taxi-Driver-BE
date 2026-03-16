package com.driving.backend.service;

import com.driving.backend.dto.map.CoordinatePoint;
import com.driving.backend.dto.map.SegmentCenter;
import com.driving.backend.dto.map.VulnerableSegmentItem;
import com.driving.backend.dto.map.VulnerableSegmentsResponse;
import com.driving.backend.entity.RoadSegment;
import com.driving.backend.entity.SegmentVulnerabilityMap;
import com.driving.backend.entity.UserVulnerabilityMap;
import com.driving.backend.exception.InvalidRequestException;
import com.driving.backend.exception.InvalidTokenException;
import com.driving.backend.repository.RoadSegmentRepository;
import com.driving.backend.repository.SegmentVulnerabilityMapRepository;
import com.driving.backend.repository.UserVulnerabilityMapRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements business logic by coordinating domain and repository data.
 */
@Service
public class MapVulnerableService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final UserVulnerabilityMapRepository userVulnerabilityMapRepository;
    private final RoadSegmentRepository roadSegmentRepository;
    private final SegmentVulnerabilityMapRepository segmentVulnerabilityMapRepository;
    private final ObjectMapper objectMapper;

    public MapVulnerableService(
            JwtTokenService jwtTokenService,
            UserVulnerabilityMapRepository userVulnerabilityMapRepository,
            RoadSegmentRepository roadSegmentRepository,
            SegmentVulnerabilityMapRepository segmentVulnerabilityMapRepository,
            ObjectMapper objectMapper
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userVulnerabilityMapRepository = userVulnerabilityMapRepository;
        this.roadSegmentRepository = roadSegmentRepository;
        this.segmentVulnerabilityMapRepository = segmentVulnerabilityMapRepository;
        this.objectMapper = objectMapper;
    }

    public VulnerableSegmentsResponse getVulnerableSegments(
            String authorizationHeader,
            String minLatRaw,
            String minLonRaw,
            String maxLatRaw,
            String maxLonRaw,
            String minSeverityRaw,
            String returnModeRaw,
            String levelsRaw
    ) {
        Long userId = extractUserId(authorizationHeader);

        double minLat = parseDouble(minLatRaw);
        double minLon = parseDouble(minLonRaw);
        double maxLat = parseDouble(maxLatRaw);
        double maxLon = parseDouble(maxLonRaw);
        validateBounds(minLat, minLon, maxLat, maxLon);

        double minSeverity = parseMinSeverity(minSeverityRaw);
        boolean includeCoordinates = isSegmentMode(returnModeRaw);
        Set<Integer> levelFilter = parseLevels(levelsRaw);

        List<Integer> vulnerabilityTypeIds = userVulnerabilityMapRepository
                .findByUserIdOrderByVulnerabilityTypeIdAsc(userId)
                .stream()
                .map(UserVulnerabilityMap::getVulnerabilityTypeId)
                .toList();

        if (vulnerabilityTypeIds.isEmpty()) {
            return new VulnerableSegmentsResponse(userId, vulnerabilityTypeIds, 0, List.of());
        }

        List<RoadSegment> segmentsInBounds = roadSegmentRepository
                .findByCenterLatBetweenAndCenterLonBetween(minLat, maxLat, minLon, maxLon);

        if (segmentsInBounds.isEmpty()) {
            return new VulnerableSegmentsResponse(userId, vulnerabilityTypeIds, 0, List.of());
        }

        Map<String, RoadSegment> roadById = segmentsInBounds.stream()
                .filter(segment -> segment.getLevel() != null && segment.getTotalScore() != null)
                .filter(segment -> levelFilter.isEmpty() || levelFilter.contains(segment.getLevel()))
                .collect(Collectors.toMap(RoadSegment::getSegmentId, segment -> segment, (left, right) -> left));

        if (roadById.isEmpty()) {
            return new VulnerableSegmentsResponse(userId, vulnerabilityTypeIds, 0, List.of());
        }

        List<String> candidateSegmentIds = new ArrayList<>(roadById.keySet());
        List<SegmentVulnerabilityMap> matched = segmentVulnerabilityMapRepository
                .findByVulnerabilityTypeIdInAndSegmentIdInAndSeverityGreaterThanEqual(
                        vulnerabilityTypeIds,
                        candidateSegmentIds,
                        minSeverity
                );

        List<VulnerableSegmentItem> items = matched.stream()
                .map(row -> toItem(row, roadById.get(row.getId().getSegmentId()), includeCoordinates))
                .filter(item -> item != null)
                .toList();

        return new VulnerableSegmentsResponse(userId, vulnerabilityTypeIds, items.size(), items);
    }

    private VulnerableSegmentItem toItem(
            SegmentVulnerabilityMap row,
            RoadSegment road,
            boolean includeCoordinates
    ) {
        if (road == null) {
            return null;
        }

        List<CoordinatePoint> coordinates = includeCoordinates
                ? parseCoordinates(road.getCoordinatesJson())
                : null;

        return new VulnerableSegmentItem(
                row.getId().getSegmentId(),
                row.getId().getVulnerabilityTypeId(),
                row.getSeverity(),
                row.getNote(),
                row.getSource(),
                road.getLevel(),
                road.getLevelText(),
                road.getTotalScore(),
                road.getExplanation(),
                new SegmentCenter(valueOrZero(road.getCenterLat()), valueOrZero(road.getCenterLon())),
                coordinates
        );
    }

    private boolean isSegmentMode(String returnModeRaw) {
        if (!StringUtils.hasText(returnModeRaw)) {
            return false;
        }
        return "SEGMENT".equalsIgnoreCase(returnModeRaw.trim());
    }

    private double parseMinSeverity(String minSeverityRaw) {
        if (!StringUtils.hasText(minSeverityRaw)) {
            return 0.0;
        }

        try {
            return Double.parseDouble(minSeverityRaw);
        } catch (Exception ex) {
            throw new InvalidRequestException("Invalid bounds");
        }
    }

    private Set<Integer> parseLevels(String levelsRaw) {
        if (!StringUtils.hasText(levelsRaw)) {
            return Collections.emptySet();
        }

        String[] tokens = levelsRaw.split(",");
        Set<Integer> result = new LinkedHashSet<>();
        for (String token : tokens) {
            try {
                result.add(Integer.parseInt(token.trim()));
            } catch (Exception ex) {
                throw new InvalidRequestException("Invalid bounds");
            }
        }
        return result;
    }

    private Long extractUserId(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidTokenException("Invalid token");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new InvalidTokenException("Invalid token");
        }

        return jwtTokenService.extractUserId(token);
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            throw new InvalidRequestException("Invalid bounds");
        }
    }

    private void validateBounds(double minLat, double minLon, double maxLat, double maxLon) {
        if (Double.compare(minLat, maxLat) >= 0 || Double.compare(minLon, maxLon) >= 0) {
            throw new InvalidRequestException("Invalid bounds");
        }
    }

    private List<CoordinatePoint> parseCoordinates(String coordinatesJson) {
        if (!StringUtils.hasText(coordinatesJson)) {
            return List.of();
        }

        try {
            List<List<Double>> raw = objectMapper.readValue(coordinatesJson, new TypeReference<>() {
            });
            List<CoordinatePoint> points = new ArrayList<>();
            for (List<Double> pair : raw) {
                if (pair.size() < 2) {
                    continue;
                }
                points.add(new CoordinatePoint(pair.get(1), pair.get(0)));
            }
            return points;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }
}
