package com.driving.backend.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurveyScoringSupportTest {

    @Test
    void evaluatesLowRiskDriverAsHighSkill() {
        Map<String, Integer> answers = SurveyScoringSupport.questions().stream()
                .collect(Collectors.toMap(
                        SurveyScoringSupport.SurveyQuestionDefinition::code,
                        question -> question.reverseScored() ? 5 : 1
                ));

        SurveyScoringSupport.SurveyEvaluation evaluation = SurveyScoringSupport.evaluate(answers);

        assertEquals(100, evaluation.skillLevel());
        assertTrue(evaluation.vulnerabilityCodes().isEmpty());
        assertNull(evaluation.primaryVulnerabilityCode());
    }

    @Test
    void evaluatesHighRiskDriverWithMappedVulnerabilities() {
        Map<String, Integer> answers = SurveyScoringSupport.questions().stream()
                .collect(Collectors.toMap(
                        SurveyScoringSupport.SurveyQuestionDefinition::code,
                        question -> question.reverseScored() ? 1 : 5
                ));

        SurveyScoringSupport.SurveyEvaluation evaluation = SurveyScoringSupport.evaluate(answers);

        assertEquals(0, evaluation.skillLevel());
        List<String> expectedVulnerabilityCodes = SurveyScoringSupport.questions().stream()
                .flatMap(question -> question.vulnerabilityCodes().stream())
                .distinct()
                .sorted()
                .toList();
        assertEquals(
                expectedVulnerabilityCodes,
                evaluation.vulnerabilityCodes().stream()
                        .sorted()
                        .toList()
        );
        assertEquals("AVOID_ACCIDENT_PRONE", evaluation.primaryVulnerabilityCode());
    }
}
