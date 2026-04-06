package com.driving.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내 프로필 및 설문 요약 응답")
public record UserProfileResponse(
    @Schema(description = "사용자 ID", example = "1")
    @JsonProperty("user_id") Long userId,
    @Schema(description = "이메일", example = "test@test.com")
    @JsonProperty("email") String email,
    @Schema(description = "닉네임", example = "테스트유저")
    @JsonProperty("nickname") String nickname,
    @Schema(description = "설문 기반 숙련도 점수", example = "42")
    @JsonProperty("skill_level") Integer skillLevel,
    @Schema(description = "대표 취약특성 ID", example = "1")
    @JsonProperty("primary_vulnerability_type_id") Integer primaryVulnerabilityTypeId,
    @Schema(description = "취약특성 ID 목록", example = "[1,3]")
    @JsonProperty("vulnerability_type_ids") List<Integer> vulnerabilityTypeIds,
    @JsonProperty("vulnerability_types") List<VulnerabilityTypeDetail> vulnerabilityTypes,
    @Schema(description = "회원 생성 시각", example = "2026-04-06T18:30:00")
    @JsonProperty("created_at") LocalDateTime createdAt,
    @Schema(description = "최근 설문 또는 프로필 업데이트 시각", example = "2026-04-06T19:12:00")
    @JsonProperty("updated_at") LocalDateTime updatedAt
) {
}

