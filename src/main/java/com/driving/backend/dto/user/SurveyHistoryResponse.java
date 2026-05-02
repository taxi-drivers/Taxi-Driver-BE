package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SurveyHistoryResponse(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("histories") List<SurveyHistoryItemResponse> histories
) {
}
