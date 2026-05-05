package com.driving.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AdminUserListResponse(
        @JsonProperty("count") int count,
        @JsonProperty("users") List<AdminUserSummaryResponse> users
) {
}
