package com.driving.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Defines application-level configuration and security-related settings.
 */
@ConfigurationProperties(prefix = "app.auth")
public record JwtProperties(
        String jwtSecret,
        long accessTokenMinutes,
        long refreshTokenDays
) {
}

