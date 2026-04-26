package com.driving.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Defines application-level configuration and security-related settings.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
}

