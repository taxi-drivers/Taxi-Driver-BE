package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "설문 결과 저장 요청. answers를 보내면 서버가 자동 채점하고, 직접 계산값을 넣어도 됩니다.")
public record SubmitSurveyRequest(
    @Schema(description = "직접 계산한 숙련도 점수", example = "42", nullable = true)
    @JsonProperty("skill_level") Integer skillLevel,
    @Schema(description = "취약특성 ID 목록", example = "[1,3]", nullable = true)
    @JsonProperty("vulnerability_type_ids") List<Integer> vulnerabilityTypeIds,
    @Schema(description = "대표 취약특성 ID", example = "1", nullable = true)
    @JsonProperty("primary_vulnerability_type_id") Integer primaryVulnerabilityTypeId,
    @Schema(description = "설문 답안 맵. 키는 question code, 값은 1~5", example = "{\"ROAD_FORM_HIGHWAY_STRESS\":4,\"ROAD_SCALE_PREFER_WIDE_ROAD\":5}")
    @JsonProperty("answers") Map<String, Integer> answers,
    @Schema(description = "클라이언트 버전", example = "web-1.0.0", nullable = true)
    @JsonProperty("client_version") String clientVersion
) {
}

