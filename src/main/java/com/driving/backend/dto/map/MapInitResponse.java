package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "메인 지도 초기화 응답")
public record MapInitResponse(
    @JsonProperty("user") MapInitUser user,
    @Schema(description = "사용자가 선택한 취약특성 ID 목록", example = "[1,3]")
    @JsonProperty("user_vulnerability_type_ids") List<Integer> userVulnerabilityTypeIds,
    @JsonProperty("level_rule") MapInitLevelRule levelRule,
    @JsonProperty("color_scheme") Map<String, String> colorScheme,
    @JsonProperty("vulnerability_types") List<MapInitVulnerabilityType> vulnerabilityTypes,
    @JsonProperty("segments") List<MapSegmentItem> segments
) {
}

