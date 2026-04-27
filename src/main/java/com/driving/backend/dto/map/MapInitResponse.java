package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record MapInitResponse(
    @JsonProperty("user") MapInitUser user,
    @JsonProperty("user_vulnerability_type_ids") List<Integer> userVulnerabilityTypeIds,
    @JsonProperty("level_rule") MapInitLevelRule levelRule,
    @JsonProperty("color_scheme") Map<String, String> colorScheme,
    @JsonProperty("vulnerability_types") List<MapInitVulnerabilityType> vulnerabilityTypes,
    @JsonProperty("segments") List<MapSegmentItem> segments
) {
}

