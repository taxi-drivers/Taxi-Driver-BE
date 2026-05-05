package com.driving.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AdminSurveyHistoryResponse(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("histories") List<AdminSurveyHistoryItemResponse> histories
) {
}
