package com.driving.backend.service;

import com.driving.backend.exception.InvalidRequestException;

import java.util.List;
import java.util.Map;
import java.util.Set;

final class SurveyScoringSupport {

    static final String SURVEY_VERSION = "survey-v3";

    private static final int MIN_OPTION_VALUE = 1;
    private static final int MAX_OPTION_VALUE = 5;
    private static final double VULNERABILITY_THRESHOLD = 0.6d;
    private static final double FALLBACK_PRIMARY_THRESHOLD = 0.45d;

    private static final List<SurveyQuestionDefinition> QUESTIONS = List.of(
            new SurveyQuestionDefinition("ROAD_FORM_HIGHWAY_STRESS", "도로형태", "고속도로나 자동차 전용도로 주행 시 심리적 부담을 많이 느낀다.", false, List.of("AVOID_HIGHWAY")),
            new SurveyQuestionDefinition("ROAD_SCALE_PREFER_WIDE_ROAD", "도로 규모", "차선이 좁은 도로(6m 이하)보다 왕복 4차로 이상의 넓은 도로를 선호한다.", false, List.of("PREFER_WIDE_ROAD")),
            new SurveyQuestionDefinition("INTERSECTION_COMPLEX_DIFFICULTY", "교차", "복잡한 오거리나 비정형 교차로에서 경로를 찾는 데 어려움을 느낀다.", false, List.of("AVOID_COMPLEX_INTERSECTION")),
            new SurveyQuestionDefinition("DRIVING_ENV_BACKSTREET_STRESS", "주행환경", "이면도로(골목길) 주행 시 불법 주정차 차량으로 인한 스트레스가 크다.", false, List.of("PREFER_WIDE_ROAD")),
            new SurveyQuestionDefinition("DRIVING_HABIT_HARSH_BRAKING", "운전 습관", "평소 급가속이나 급제동을 자주 하는 편이다.", false, List.of("AVOID_ACCIDENT_PRONE")),
            new SurveyQuestionDefinition("WEATHER_ICY_CONFIDENCE", "기상", "결빙 구간에서의 운전을 경험해봤거나 운전에 자신이 있다.", true, List.of("AVOID_STEEP_SLOPE")),
            new SurveyQuestionDefinition("PEDESTRIAN_SAFETY_AVOID_BUSY_ZONE", "보행 안전", "보행자가 많은 어린이 보호구역이나 시장통 주행을 기피한다.", false, List.of("AVOID_COMPLEX_INTERSECTION")),
            new SurveyQuestionDefinition("INCIDENT_RESPONSE_SLOW", "돌발 상황", "예기치 못한 공사 구간이나 사고 구간 인지 시 대처가 느린 편이다.", false, List.of("AVOID_ACCIDENT_PRONE"))
    );

    private SurveyScoringSupport() {
    }

    static List<SurveyQuestionDefinition> questions() {
        return QUESTIONS;
    }

    /**
     * 응답 Map에서 vulnerability별 평균 risk strength (0~1) 계산.
     * 임계값(threshold) 통과 여부와 무관하게 모든 vulnerability에 대해 strength를 산출.
     * 경로 페널티 가중치로 직접 사용된다.
     */
    static java.util.Map<String, Double> computeStrengths(java.util.Map<String, Integer> answers) {
        if (answers == null || answers.isEmpty()) {
            return java.util.Map.of();
        }

        java.util.Map<String, Double> sums = new java.util.LinkedHashMap<>();
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();

        for (SurveyQuestionDefinition q : QUESTIONS) {
            Integer answer = answers.get(q.code());
            if (answer == null || answer < MIN_OPTION_VALUE || answer > MAX_OPTION_VALUE) {
                continue;
            }
            double normalizedSkill = q.reverseScored()
                    ? (answer - MIN_OPTION_VALUE) / 4d
                    : (MAX_OPTION_VALUE - answer) / 4d;
            double risk = 1d - normalizedSkill;

            for (String code : q.vulnerabilityCodes()) {
                sums.merge(code, risk, Double::sum);
                counts.merge(code, 1, Integer::sum);
            }
        }

        java.util.Map<String, Double> result = new java.util.LinkedHashMap<>();
        for (var entry : sums.entrySet()) {
            int count = counts.getOrDefault(entry.getKey(), 1);
            result.put(entry.getKey(), entry.getValue() / count);
        }
        return result;
    }

    static SurveyEvaluation evaluate(Map<String, Integer> answers) {
        validateAnswers(answers);

        double skillSum = 0d;
        Map<String, Double> vulnerabilityRiskScores = new java.util.LinkedHashMap<>();
        Map<String, Integer> vulnerabilityCounts = new java.util.LinkedHashMap<>();

        for (SurveyQuestionDefinition question : QUESTIONS) {
            int answerValue = answers.get(question.code());
            double normalizedSkill = question.reverseScored()
                    ? (answerValue - MIN_OPTION_VALUE) / 4d
                    : (MAX_OPTION_VALUE - answerValue) / 4d;
            double normalizedRisk = 1d - normalizedSkill;

            skillSum += normalizedSkill;

            for (String vulnerabilityCode : question.vulnerabilityCodes()) {
                vulnerabilityRiskScores.merge(vulnerabilityCode, normalizedRisk, Double::sum);
                vulnerabilityCounts.merge(vulnerabilityCode, 1, Integer::sum);
            }
        }

        int skillLevel = (int) Math.round((skillSum / QUESTIONS.size()) * 100d);

        List<Map.Entry<String, Double>> rankedVulnerabilities = vulnerabilityRiskScores.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue() / vulnerabilityCounts.getOrDefault(entry.getKey(), 1)))
                .sorted((left, right) -> {
                    int byRisk = Double.compare(right.getValue(), left.getValue());
                    if (byRisk != 0) {
                        return byRisk;
                    }

                    int byQuestionCount = Integer.compare(
                            vulnerabilityCounts.getOrDefault(right.getKey(), 0),
                            vulnerabilityCounts.getOrDefault(left.getKey(), 0)
                    );
                    if (byQuestionCount != 0) {
                        return byQuestionCount;
                    }

                    return left.getKey().compareTo(right.getKey());
                })
                .toList();

        List<String> vulnerabilityCodes = rankedVulnerabilities.stream()
                .filter(entry -> entry.getValue() >= VULNERABILITY_THRESHOLD)
                .map(Map.Entry::getKey)
                .toList();

        String primaryVulnerabilityCode = rankedVulnerabilities.isEmpty() ? null : rankedVulnerabilities.get(0).getKey();
        List<String> finalVulnerabilityCodes = vulnerabilityCodes;
        if (finalVulnerabilityCodes.isEmpty() && !rankedVulnerabilities.isEmpty()
                && rankedVulnerabilities.get(0).getValue() >= FALLBACK_PRIMARY_THRESHOLD) {
            finalVulnerabilityCodes = List.of(primaryVulnerabilityCode);
        }

        if (finalVulnerabilityCodes.isEmpty()) {
            primaryVulnerabilityCode = null;
        }

        return new SurveyEvaluation(skillLevel, finalVulnerabilityCodes, primaryVulnerabilityCode);
    }

    private static void validateAnswers(Map<String, Integer> answers) {
        if (answers == null || answers.size() != QUESTIONS.size()) {
            throw new InvalidRequestException("Invalid survey answers");
        }

        Set<String> expectedCodes = QUESTIONS.stream()
                .map(SurveyQuestionDefinition::code)
                .collect(java.util.stream.Collectors.toSet());

        if (!answers.keySet().equals(expectedCodes)) {
            throw new InvalidRequestException("Invalid survey answers");
        }

        boolean hasInvalidValue = answers.values().stream()
                .anyMatch(value -> value == null || value < MIN_OPTION_VALUE || value > MAX_OPTION_VALUE);
        if (hasInvalidValue) {
            throw new InvalidRequestException("Invalid survey answers");
        }
    }

    record SurveyQuestionDefinition(
            String code,
            String category,
            String prompt,
            boolean reverseScored,
            List<String> vulnerabilityCodes
    ) {
    }

    record SurveyEvaluation(
            int skillLevel,
            List<String> vulnerabilityCodes,
            String primaryVulnerabilityCode
    ) {
    }
}
