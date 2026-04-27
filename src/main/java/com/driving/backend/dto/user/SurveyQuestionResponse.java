package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SurveyQuestionResponse(
        @JsonProperty("code") String code,
        @JsonProperty("category") String category,
        @JsonProperty("prompt") String prompt,
        @JsonProperty("reverse_scored") boolean reverseScored,
        @JsonProperty("options") List<SurveyQuestionOptionResponse> options
) {
}
