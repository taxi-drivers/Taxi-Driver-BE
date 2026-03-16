package com.driving.backend.service;

import com.driving.backend.dto.auth.LoginRequest;
import com.driving.backend.dto.auth.LoginResponse;
import com.driving.backend.dto.auth.LogoutResponse;
import com.driving.backend.entity.User;
import com.driving.backend.entity.UserProfile;
import com.driving.backend.exception.InvalidCredentialsException;
import com.driving.backend.exception.InvalidRequestException;
import com.driving.backend.exception.InvalidTokenException;
import com.driving.backend.repository.UserProfileRepository;
import com.driving.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        validateRequest(request);

        User user = userRepository.findByEmail(request.email().trim())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordMatches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserProfile profile = userProfileRepository.findById(user.getUserId()).orElse(null);
        String accessToken = jwtTokenService.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenService.createRefreshToken(user.getUserId());

        return new LoginResponse(
                user.getUserId(),
                user.getNickname(),
                profile != null ? profile.getSkillLevel() : null,
                profile != null && profile.getVulnerabilityType() != null
                        ? profile.getVulnerabilityType().getVulnerabilityTypeId()
                        : null,
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public LogoutResponse logout(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidTokenException("Invalid token");
        }

        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(accessToken) || !jwtTokenService.isValidToken(accessToken)) {
            throw new InvalidTokenException("Invalid token");
        }

        return new LogoutResponse("Logged out successfully");
    }

    private void validateRequest(LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.email()) || !StringUtils.hasText(request.password())) {
            throw new InvalidRequestException("Invalid request body");
        }
    }

    private boolean passwordMatches(String rawPassword, String storedPasswordHash) {
        if (!StringUtils.hasText(storedPasswordHash)) {
            return false;
        }

        try {
            return org.springframework.security.crypto.bcrypt.BCrypt.checkpw(rawPassword, storedPasswordHash);
        } catch (Exception ignored) {
            return false;
        }
    }
}
