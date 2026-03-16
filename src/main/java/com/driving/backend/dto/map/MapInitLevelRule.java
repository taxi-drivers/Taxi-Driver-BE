package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record MapInitLevelRule(
    @JsonProperty("level_rule_id") Long levelRuleId,
    @JsonProperty("version") String version,
    @JsonProperty("name") String name,
    @JsonProperty("level1_max") Double level1Max,
    @JsonProperty("level2_max") Double level2Max
) {
}

