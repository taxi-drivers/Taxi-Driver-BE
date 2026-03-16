package com.driving.backend.service;

import com.driving.backend.dto.user.UpdateNicknameRequest;
import com.driving.backend.dto.user.UpdateProfileResponse;
import com.driving.backend.dto.user.UserProfileResponse;
import com.driving.backend.dto.user.VulnerabilityTypeDetail;
import com.driving.backend.entity.User;
import com.driving.backend.entity.UserProfile;
import com.driving.backend.entity.UserVulnerabilityMap;
import com.driving.backend.entity.VulnerabilityType;
import com.driving.backend.exception.InvalidRequestException;
import com.driving.backend.exception.InvalidTokenException;
import com.driving.backend.exception.UserNotFoundException;
import com.driving.backend.repository.UserProfileRepository;
import com.driving.backend.repository.UserRepository;
import com.driving.backend.repository.UserVulnerabilityMapRepository;
import com.driving.backend.repository.VulnerabilityTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class UserProfileService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserVulnerabilityMapRepository userVulnerabilityMapRepository;
    private final VulnerabilityTypeRepository vulnerabilityTypeRepository;

    public UserProfileService(
            JwtTokenService jwtTokenService,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserVulnerabilityMapRepository userVulnerabilityMapRepository,
            VulnerabilityTypeRepository vulnerabilityTypeRepository
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userVulnerabilityMapRepository = userVulnerabilityMapRepository;
        this.vulnerabilityTypeRepository = vulnerabilityTypeRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String authorizationHeader) {
        Long userId = extractUserId(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User profile not found"));

        List<Integer> vulnerabilityTypeIds = userVulnerabilityMapRepository
                .findByUserIdOrderByVulnerabilityTypeIdAsc(userId)
                .stream()
                .map(UserVulnerabilityMap::getVulnerabilityTypeId)
                .toList();

        List<VulnerabilityTypeDetail> vulnerabilityTypes = vulnerabilityTypeIds.isEmpty()
                ? Collections.emptyList()
                : vulnerabilityTypeRepository
                .findByVulnerabilityTypeIdInOrderByVulnerabilityTypeIdAsc(vulnerabilityTypeIds)
                .stream()
                .map(this::toDetail)
                .toList();

        return new UserProfileResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                profile.getSkillLevel(),
                profile.getVulnerabilityType() != null ? profile.getVulnerabilityType().getVulnerabilityTypeId() : null,
                vulnerabilityTypeIds,
                vulnerabilityTypes,
                user.getCreatedAt(),
                profile.getUpdatedAt() != null ? profile.getUpdatedAt() : user.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<String> getMyVulnerabilityCodes(String authorizationHeader) {
        Long userId = extractUserId(authorizationHeader);

        List<Integer> vulnerabilityTypeIds = userVulnerabilityMapRepository
                .findByUserIdOrderByVulnerabilityTypeIdAsc(userId)
                .stream()
                .map(UserVulnerabilityMap::getVulnerabilityTypeId)
                .toList();

        if (vulnerabilityTypeIds.isEmpty()) {
            return List.of();
        }

        return vulnerabilityTypeRepository
                .findByVulnerabilityTypeIdInOrderByVulnerabilityTypeIdAsc(vulnerabilityTypeIds)
                .stream()
                .map(VulnerabilityType::getCode)
                .toList();
    }

    @Transactional
    public UpdateProfileResponse updateNickname(String authorizationHeader, UpdateNicknameRequest request) {
        Long userId = extractUserId(authorizationHeader);
        String nickname = validateNickname(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setNickname(nickname);
        User saved = userRepository.save(user);

        return new UpdateProfileResponse(
                saved.getUserId(),
                saved.getEmail(),
                saved.getNickname(),
                saved.getUpdatedAt()
        );
    }

    private String validateNickname(UpdateNicknameRequest request) {
        if (request == null || !StringUtils.hasText(request.nickname())) {
            throw new InvalidRequestException("Invalid nickname");
        }

        String nickname = request.nickname().trim();
        if (nickname.length() > 50) {
            throw new InvalidRequestException("Invalid nickname");
        }

        return nickname;
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

    private VulnerabilityTypeDetail toDetail(VulnerabilityType type) {
        return new VulnerabilityTypeDetail(
                type.getVulnerabilityTypeId(),
                type.getCode(),
                type.getName(),
                type.getDescription(),
                type.getIconKey()
        );
    }
}
