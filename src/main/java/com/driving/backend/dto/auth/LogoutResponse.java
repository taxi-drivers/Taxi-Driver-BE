package com.driving.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그아웃 응답")
public record LogoutResponse(
    @Schema(description = "처리 결과 메시지", example = "Logged out successfully")
    @JsonProperty("message") String message
) {
}

