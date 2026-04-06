package com.driving.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
    @Schema(description = "사용자 ID", example = "1")
    @JsonProperty("user_id") Long userId,
    @Schema(description = "닉네임", example = "테스트유저")
    @JsonProperty("nickname") String nickname,
    @Schema(description = "설문 기반 숙련도 점수", example = "50")
    @JsonProperty("skill_level") Integer skillLevel,
    @Schema(description = "대표 취약특성 ID", example = "1")
    @JsonProperty("primary_vulnerability_type_id") Integer vulnerabilityTypeId,
    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    @JsonProperty("access_token") String accessToken,
    @Schema(description = "JWT Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    @JsonProperty("refresh_token") String refreshToken
) {
}

