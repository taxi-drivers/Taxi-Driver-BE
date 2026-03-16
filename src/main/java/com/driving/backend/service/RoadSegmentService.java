package com.driving.backend.service;

import com.driving.backend.dto.*;
import com.driving.backend.entity.RoadSegment;
import com.driving.backend.repository.RoadSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadSegmentService {

    private final RoadSegmentRepository roadSegmentRepository;

    // GET /api/segments?minLat=&maxLat=&minLon=&maxLon=
    public List<SegmentSummaryResponse> getSegmentsByBounds(
            Double minLat, Double maxLat, Double minLon, Double maxLon) {
        return roadSegmentRepository.findByBounds(minLat, maxLat, minLon, maxLon)
                .stream()
                .map(SegmentSummaryResponse::from)
                .collect(Collectors.toList());
    }

    // GET /api/segments/{id}
    public SegmentDetailResponse getSegmentById(String segmentId) {
        RoadSegment segment = findSegmentOrThrow(segmentId);
        return SegmentDetailResponse.from(segment);
    }

    // GET /api/segments/{id}/tooltip
    public SegmentTooltipResponse getTooltip(String segmentId) {
        RoadSegment segment = findSegmentOrThrow(segmentId);
        return SegmentTooltipResponse.from(segment);
    }

    // GET /api/segments/{id}/difficulty
    public SegmentDifficultyResponse getDifficulty(String segmentId) {
        RoadSegment segment = findSegmentOrThrow(segmentId);
        return SegmentDifficultyResponse.from(segment);
    }

    // GET /api/segments/{id}/score-detail
    public SegmentScoreDetailResponse getScoreDetail(String segmentId) {
        RoadSegment segment = findSegmentOrThrow(segmentId);
        return SegmentScoreDetailResponse.from(segment);
    }

    private RoadSegment findSegmentOrThrow(String segmentId) {
        return roadSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new IllegalArgumentException("세그먼트를 찾을 수 없습니다: " + segmentId));
    }
}
