package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "설문 선택지")
public record SurveyQuestionOptionResponse(
        @Schema(description = "선택값", example = "1")
        @JsonProperty("value") Integer value,
        @Schema(description = "화면 표시 라벨", example = "전혀 아니다")
        @JsonProperty("label") String label
) {
}
