package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "프로필 수정 결과")
public record UpdateProfileResponse(
    @Schema(description = "사용자 ID", example = "1")
    @JsonProperty("user_id") Long userId,
    @Schema(description = "이메일", example = "test@test.com")
    @JsonProperty("email") String email,
    @Schema(description = "변경된 닉네임", example = "초보운전자")
    @JsonProperty("nickname") String nickname,
    @Schema(description = "업데이트 시각", example = "2026-04-06T19:20:00")
    @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}

