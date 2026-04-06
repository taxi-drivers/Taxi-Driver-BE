package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "설문 저장 결과")
public record SubmitSurveyResponse(
    @Schema(description = "사용자 ID", example = "1")
    @JsonProperty("user_id") Long userId,
    @Schema(description = "저장된 숙련도 점수", example = "42")
    @JsonProperty("skill_level") Integer skillLevel,
    @Schema(description = "저장된 취약특성 ID 목록", example = "[1,3]")
    @JsonProperty("vulnerability_type_ids") List<Integer> vulnerabilityTypeIds,
    @Schema(description = "대표 취약특성 ID", example = "1")
    @JsonProperty("primary_vulnerability_type_id") Integer primaryVulnerabilityTypeId,
    @Schema(description = "업데이트 시각", example = "2026-04-06T19:12:00")
    @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}

