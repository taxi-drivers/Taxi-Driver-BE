package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SurveyQuestionsResponse(
        @JsonProperty("survey_version") String surveyVersion,
        @JsonProperty("questions") List<SurveyQuestionResponse> questions
) {
}
