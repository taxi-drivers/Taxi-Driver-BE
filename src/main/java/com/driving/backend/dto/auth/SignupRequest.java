package com.driving.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SignupRequest(
        @JsonProperty("email") String email,
        @JsonProperty("password") String password,
        @JsonProperty("nickname") String nickname
) {
}
