package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SurveyQuestionOptionResponse(
        @JsonProperty("value") Integer value,
        @JsonProperty("label") String label
) {
}
