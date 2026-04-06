package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 닉네임 수정 요청")
public record UpdateNicknameRequest(
    @Schema(description = "새 닉네임", example = "초보운전자", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("nickname") String nickname
) {
}

