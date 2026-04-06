package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "설문 문항")
public record SurveyQuestionResponse(
        @Schema(description = "문항 코드", example = "ROAD_FORM_HIGHWAY_STRESS")
        @JsonProperty("code") String code,
        @Schema(description = "문항 카테고리", example = "도로형태")
        @JsonProperty("category") String category,
        @Schema(description = "문항 질문", example = "고속도로나 자동차 전용도로 주행 시 심리적 부담을 많이 느낀다.")
        @JsonProperty("prompt") String prompt,
        @Schema(description = "역채점 여부", example = "false")
        @JsonProperty("reverse_scored") boolean reverseScored,
        @JsonProperty("options") List<SurveyQuestionOptionResponse> options
) {
}
