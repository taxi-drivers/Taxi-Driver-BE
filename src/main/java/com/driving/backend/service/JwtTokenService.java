package com.driving.backend.service;

import com.driving.backend.config.JwtProperties;
import com.driving.backend.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Implements business logic by coordinating domain and repository data.
 */
@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plus(jwtProperties.accessTokenMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .claim("user_id", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plus(jwtProperties.refreshTokenDays(), ChronoUnit.DAYS);

        return Jwts.builder()
                .claim("user_id", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public boolean isValidToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        try {
            Claims claims = parseClaims(token);
            Object rawUserId = claims.get("user_id");
            if (rawUserId instanceof Number number) {
                return number.longValue();
            }
        } catch (Exception ignored) {

        }
        throw new InvalidTokenException("Invalid token");
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

