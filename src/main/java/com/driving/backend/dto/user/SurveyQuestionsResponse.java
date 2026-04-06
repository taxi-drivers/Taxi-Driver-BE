package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "설문 문항 조회 응답")
public record SurveyQuestionsResponse(
        @Schema(description = "설문 버전", example = "survey-v2")
        @JsonProperty("survey_version") String surveyVersion,
        @JsonProperty("questions") List<SurveyQuestionResponse> questions
) {
}
