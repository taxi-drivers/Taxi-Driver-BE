package com.driving.backend.service;

import com.driving.backend.dto.user.SubmitSurveyRequest;
import com.driving.backend.dto.user.SubmitSurveyResponse;
import com.driving.backend.entity.User;
import com.driving.backend.entity.UserVulnerabilityMap;
import com.driving.backend.exception.InvalidRequestException;
import com.driving.backend.exception.InvalidTokenException;
import com.driving.backend.exception.UserNotFoundException;
import com.driving.backend.repository.UserRepository;
import com.driving.backend.repository.UserVulnerabilityMapRepository;
import com.driving.backend.repository.VulnerabilityTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements business logic by coordinating domain and repository data.
 */
@Service
public class UserSurveyService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final UserVulnerabilityMapRepository userVulnerabilityMapRepository;
    private final VulnerabilityTypeRepository vulnerabilityTypeRepository;

    public UserSurveyService(
            JwtTokenService jwtTokenService,
            UserRepository userRepository,
            UserVulnerabilityMapRepository userVulnerabilityMapRepository,
            VulnerabilityTypeRepository vulnerabilityTypeRepository
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.userVulnerabilityMapRepository = userVulnerabilityMapRepository;
        this.vulnerabilityTypeRepository = vulnerabilityTypeRepository;
    }

    @Transactional
    public SubmitSurveyResponse submitSurvey(String authorizationHeader, SubmitSurveyRequest request) {
        Long userId = extractUserId(authorizationHeader);

        validateRequest(request);

        List<Integer> normalizedVulnerabilityIds = normalizeVulnerabilityIds(request.vulnerabilityTypeIds());
        validateVulnerabilityTypeIds(normalizedVulnerabilityIds);
        validatePrimaryVulnerabilityTypeId(request.primaryVulnerabilityTypeId(), normalizedVulnerabilityIds);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setSkillLevel(request.skillLevel());
        user.setVulnerabilityTypeId(request.primaryVulnerabilityTypeId());
        User savedUser = userRepository.save(user);

        userVulnerabilityMapRepository.deleteByUserId(userId);

        List<UserVulnerabilityMap> mappings = normalizedVulnerabilityIds.stream()
                .map(vulnerabilityId -> UserVulnerabilityMap.builder()
                        .userId(userId)
                        .vulnerabilityTypeId(vulnerabilityId)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        if (!mappings.isEmpty()) {
            userVulnerabilityMapRepository.saveAll(mappings);
        }

        return new SubmitSurveyResponse(
                savedUser.getUserId(),
                savedUser.getSkillLevel(),
                normalizedVulnerabilityIds,
                savedUser.getVulnerabilityTypeId(),
                savedUser.getUpdatedAt()
        );
    }

    private void validateRequest(SubmitSurveyRequest request) {
        if (request == null || request.skillLevel() == null || request.vulnerabilityTypeIds() == null) {
            throw new InvalidRequestException("Invalid request body");
        }

        if (request.skillLevel() < 0 || request.skillLevel() > 100) {
            throw new InvalidRequestException("Invalid request body");
        }
    }

    private List<Integer> normalizeVulnerabilityIds(List<Integer> vulnerabilityTypeIds) {
        if (vulnerabilityTypeIds.stream().anyMatch(v -> v == null)) {
            throw new InvalidRequestException("Invalid request body");
        }

        Set<Integer> unique = new LinkedHashSet<>(vulnerabilityTypeIds);
        return List.copyOf(unique);
    }

    private void validateVulnerabilityTypeIds(List<Integer> vulnerabilityTypeIds) {
        if (vulnerabilityTypeIds.isEmpty()) {
            return;
        }

        int validCount = vulnerabilityTypeRepository
                .findByVulnerabilityTypeIdInOrderByVulnerabilityTypeIdAsc(vulnerabilityTypeIds)
                .size();

        if (validCount != vulnerabilityTypeIds.size()) {
            throw new InvalidRequestException("Invalid request body");
        }
    }

    private void validatePrimaryVulnerabilityTypeId(Integer primaryVulnerabilityTypeId, List<Integer> vulnerabilityTypeIds) {
        if (primaryVulnerabilityTypeId == null) {
            return;
        }

        if (!vulnerabilityTypeIds.contains(primaryVulnerabilityTypeId)) {
            throw new InvalidRequestException("Invalid request body");
        }
    }

    private Long extractUserId(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidTokenException("Invalid token");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new InvalidTokenException("Invalid token");
        }

        return jwtTokenService.extractUserId(token);
    }
}

