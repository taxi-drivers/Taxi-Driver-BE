package com.driving.backend.dto.map;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 사용자가 지도에서 드래그한 영역의 도로 분석 응답.
 *
 *  - edgeCount: 영역 내 엣지 수
 *  - difficultyDistribution: 쉬움/보통/어려움 비율 (%)
 *  - safetyScore: 0~10 척도 (낮은 난이도일수록 높은 안전 점수)
 *  - avgDifficulty: 평균 난이도 (0~100)
 *  - warningIndicators: 위험 카테고리별 카운트
 *  - summary: 자연어 요약 (LLM 미연결 시 템플릿 텍스트)
 */
@Getter
@Builder
public class AreaSummaryResponse {
    private int edgeCount;
    private DifficultyDistribution difficultyDistribution;
    private double avgDifficulty;
    private double safetyScore;
    private List<WarningIndicator> warningIndicators;
    private String summary;

    @Getter
    @Builder
    public static class DifficultyDistribution {
        private int low;        // 카운트
        private int mid;
        private int high;
        private double lowPct;  // 비율 (%)
        private double midPct;
        private double highPct;
    }

    @Getter
    @Builder
    public static class WarningIndicator {
        private String type;        // "complex_intersection", "narrow_road", "service_road", "high_traffic", "accident_prone", "steep_slope"
        private String label;       // 한국어 라벨
        private int count;
        private String description; // 짧은 설명
    }
}
