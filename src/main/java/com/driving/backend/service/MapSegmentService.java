package com.driving.backend.service;

import com.driving.backend.dto.map.CoordinatePoint;
import com.driving.backend.dto.map.MapSegmentDetailResponse;
import com.driving.backend.dto.map.MapSegmentItem;
import com.driving.backend.dto.map.MapSegmentsResponse;
import com.driving.backend.dto.map.SegmentCenter;
import com.driving.backend.dto.map.SegmentEvidenceItem;
import com.driving.backend.dto.map.SegmentScoreBreakdown;
import com.driving.backend.dto.map.SegmentVulnerabilityDetailItem;
import com.driving.backend.dto.map.SegmentVulnerabilityDetailsResponse;
import com.driving.backend.entity.RoadSegment;
import com.driving.backend.entity.SegmentVulnerabilityMap;
import com.driving.backend.exception.InvalidRequestException;
import com.driving.backend.exception.SegmentDetailNotFoundException;
import com.driving.backend.exception.SegmentNotFoundException;
import com.driving.backend.repository.RoadSegmentRepository;
import com.driving.backend.repository.SegmentVulnerabilityMapRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements business logic by coordinating domain and repository data.
 */
@Service
public class MapSegmentService {

    private final RoadSegmentRepository roadSegmentRepository;
    private final SegmentVulnerabilityMapRepository segmentVulnerabilityMapRepository;
    private final ObjectMapper objectMapper;

    public MapSegmentService(
            RoadSegmentRepository roadSegmentRepository,
            SegmentVulnerabilityMapRepository segmentVulnerabilityMapRepository,
            ObjectMapper objectMapper
    ) {
        this.roadSegmentRepository = roadSegmentRepository;
        this.segmentVulnerabilityMapRepository = segmentVulnerabilityMapRepository;
        this.objectMapper = objectMapper;
    }

    public MapSegmentsResponse getSegments(
            String minLatRaw,
            String minLonRaw,
            String maxLatRaw,
            String maxLonRaw,
            String levelsRaw,
            String simplifiedRaw,
            String computedAfterRaw
    ) {
        double minLat = parseDouble(minLatRaw);
        double minLon = parseDouble(minLonRaw);
        double maxLat = parseDouble(maxLatRaw);
        double maxLon = parseDouble(maxLonRaw);

        validateBounds(minLat, minLon, maxLat, maxLon);

        Set<Integer> levelFilter = parseLevels(levelsRaw);
        boolean simplified = parseBooleanWithDefaultFalse(simplifiedRaw);
        LocalDateTime computedAfter = parseComputedAfter(computedAfterRaw);

        List<RoadSegment> segments = roadSegmentRepository.findByCenterLatBetweenAndCenterLonBetween(
                minLat, maxLat, minLon, maxLon
        );

        if (segments.isEmpty()) {
            return new MapSegmentsResponse(0, Collections.emptyList());
        }

        List<MapSegmentItem> items = new ArrayList<>();
        for (RoadSegment segment : segments) {
            if (segment.getLevel() == null || segment.getTotalScore() == null) {
                continue;
            }

            if (!levelFilter.isEmpty() && !levelFilter.contains(segment.getLevel())) {
                continue;
            }

            if (computedAfter != null) {
                if (segment.getComputedAt() == null || !segment.getComputedAt().isAfter(computedAfter)) {
                    continue;
                }
            }

            List<CoordinatePoint> coordinates = parseCoordinates(segment.getCoordinatesJson());
            if (simplified) {
                coordinates = simplifyCoordinates(coordinates);
            }

            items.add(new MapSegmentItem(
                    segment.getSegmentId(),
                    sanitizeName(segment.getName()),
                    segment.getHighway(),
                    new SegmentCenter(
                            valueOrZero(segment.getCenterLat()),
                            valueOrZero(segment.getCenterLon())
                    ),
                    coordinates,
                    segment.getLevel(),
                    segment.getLevelText(),
                    segment.getTotalScore(),
                    segment.getExplanation(),
                    segment.getComputedAt()
            ));
        }

        return new MapSegmentsResponse(items.size(), items);
    }

    public MapSegmentDetailResponse getSegmentDetail(String segmentIdRaw) {
        if (!StringUtils.hasText(segmentIdRaw)) {
            throw new InvalidRequestException("Invalid segment_id");
        }
        String segmentId = segmentIdRaw.trim();

        RoadSegment segment = roadSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new SegmentNotFoundException("Segment not found"));

        if (segment.getLevel() == null || segment.getTotalScore() == null) {
            throw new SegmentDetailNotFoundException("Segment detail not found");
        }

        String detailTitle = StringUtils.hasText(segment.getExplanation())
                ? segment.getExplanation()
                : "도로 상세 정보";

        return new MapSegmentDetailResponse(
                segment.getSegmentId(),
                sanitizeName(segment.getName()),
                segment.getHighway(),
                segment.getLevel(),
                segment.getLevelText(),
                segment.getTotalScore(),
                detailTitle,
                segment.getDetailDescription(),
                new SegmentScoreBreakdown(
                        segment.getAccidentRateScore(),
                        segment.getRoadShapeScore(),
                        segment.getRoadScaleScore(),
                        segment.getIntersectionScore(),
                        segment.getTrafficVolumeScore()
                ),
                parseEvidence(segment.getEvidenceJson()),
                segment.getUpdatedAt() != null ? segment.getUpdatedAt() : segment.getComputedAt()
        );
    }

    public SegmentVulnerabilityDetailsResponse getSegmentVulnerabilities(String segmentIdRaw) {
        if (!StringUtils.hasText(segmentIdRaw)) {
            throw new InvalidRequestException("Invalid segment_id");
        }
        String segmentId = segmentIdRaw.trim();

        if (!roadSegmentRepository.existsById(segmentId)) {
            throw new SegmentNotFoundException("Segment not found");
        }

        List<SegmentVulnerabilityDetailItem> items = segmentVulnerabilityMapRepository.findByIdSegmentId(segmentId).stream()
                .map(this::toVulnerabilityDetail)
                .toList();

        return new SegmentVulnerabilityDetailsResponse(segmentId, items.size(), items);
    }

    private void validateBounds(double minLat, double minLon, double maxLat, double maxLon) {
        if (Double.compare(minLat, maxLat) >= 0 || Double.compare(minLon, maxLon) >= 0) {
            throw new InvalidRequestException("Invalid bounds");
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
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
                int level = Integer.parseInt(token.trim());
                result.add(level);
            } catch (Exception ex) {
                throw new InvalidRequestException("Invalid bounds");
            }
        }
        return result;
    }

    private boolean parseBooleanWithDefaultFalse(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private LocalDateTime parseComputedAfter(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new InvalidRequestException("Invalid bounds");
        }
    }

    private List<CoordinatePoint> parseCoordinates(String coordinatesJson) {
        if (!StringUtils.hasText(coordinatesJson)) {
            return Collections.emptyList();
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
            return Collections.emptyList();
        }
    }

    private List<SegmentEvidenceItem> parseEvidence(String evidenceJson) {
        if (!StringUtils.hasText(evidenceJson)) {
            return List.of();
        }

        try {
            List<SegmentEvidenceItem> evidence = objectMapper.readValue(
                    evidenceJson,
                    new TypeReference<List<SegmentEvidenceItem>>() {
                    }
            );
            return evidence == null ? List.of() : evidence;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<CoordinatePoint> simplifyCoordinates(List<CoordinatePoint> coordinates) {
        if (coordinates.size() <= 3) {
            return coordinates;
        }

        List<CoordinatePoint> simplified = new ArrayList<>();
        simplified.add(coordinates.get(0));
        for (int index = 1; index < coordinates.size() - 1; index++) {
            if (index % 3 == 0) {
                simplified.add(coordinates.get(index));
            }
        }
        simplified.add(coordinates.get(coordinates.size() - 1));
        return simplified;
    }

    private String sanitizeName(String name) {
        if (!StringUtils.hasText(name) || "이름없음".equals(name)) {
            return null;
        }
        return name;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private SegmentVulnerabilityDetailItem toVulnerabilityDetail(SegmentVulnerabilityMap row) {
        return new SegmentVulnerabilityDetailItem(
                row.getId().getVulnerabilityTypeId(),
                row.getVulnerabilityType().getCode(),
                row.getVulnerabilityType().getName(),
                row.getVulnerabilityType().getDescription(),
                row.getVulnerabilityType().getIconKey(),
                row.getSeverity(),
                row.getNote(),
                row.getSource()
        );
    }
}
