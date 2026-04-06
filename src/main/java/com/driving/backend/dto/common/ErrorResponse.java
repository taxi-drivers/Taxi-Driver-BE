package com.driving.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 에러 응답")
public record ErrorResponse(
    @Schema(description = "HTTP 상태 코드", example = "400")
    @JsonProperty("status") int status,
    @Schema(description = "에러 메시지", example = "Invalid request body")
    @JsonProperty("message") String message
) {
}

