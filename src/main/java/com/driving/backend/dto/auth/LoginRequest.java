package com.driving.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청")
public record LoginRequest(
    @Schema(description = "로그인 이메일", example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("email") String email,
    @Schema(description = "로그인 비밀번호", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("password") String password
) {
}

