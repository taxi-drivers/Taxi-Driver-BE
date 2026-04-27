package com.driving.backend.dto.map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines request and response payload structures for API boundaries.
 */
public record SegmentEvidenceItem(
    @JsonProperty("title") String title,
    @JsonProperty("detail") String detail,
    @JsonProperty("source") String source,
    @JsonProperty("severity") Double severity
) {
}

