package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지도 초기화 시 내려주는 사용자 요약")
public record MapInitUser(
    @Schema(description = "사용자 ID", example = "1")
    @JsonProperty("user_id") Long userId,
    @Schema(description = "닉네임", example = "초보운전자")
    @JsonProperty("nickname") String nickname,
    @Schema(description = "설문 기반 숙련도", example = "42")
    @JsonProperty("skill_level") Integer skillLevel,
    @Schema(description = "대표 취약특성 ID", example = "1")
    @JsonProperty("primary_vulnerability_type_id") Integer primaryVulnerabilityTypeId
) {
}

