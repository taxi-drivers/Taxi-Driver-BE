package com.driving.backend.service;

import com.driving.backend.dto.admin.AdminSurveyHistoryItemResponse;
import com.driving.backend.dto.admin.AdminSurveyHistoryResponse;
import com.driving.backend.dto.admin.AdminUserDetailResponse;
import com.driving.backend.dto.admin.AdminUserListResponse;
import com.driving.backend.dto.admin.AdminUserSummaryResponse;
import com.driving.backend.dto.user.VulnerabilityTypeDetail;
import com.driving.backend.entity.User;
import com.driving.backend.entity.UserProfile;
import com.driving.backend.entity.UserSurveyHistory;
import com.driving.backend.entity.UserVulnerabilityMap;
import com.driving.backend.entity.VulnerabilityType;
import com.driving.backend.exception.UserNotFoundException;
import com.driving.backend.repository.UserProfileRepository;
import com.driving.backend.repository.UserRepository;
import com.driving.backend.repository.UserSurveyHistoryRepository;
import com.driving.backend.repository.UserVulnerabilityMapRepository;
import com.driving.backend.repository.VulnerabilityTypeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserService {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSurveyHistoryRepository userSurveyHistoryRepository;
    private final UserVulnerabilityMapRepository userVulnerabilityMapRepository;
    private final VulnerabilityTypeRepository vulnerabilityTypeRepository;

    public AdminUserService(
            ObjectMapper objectMapper,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserSurveyHistoryRepository userSurveyHistoryRepository,
            UserVulnerabilityMapRepository userVulnerabilityMapRepository,
            VulnerabilityTypeRepository vulnerabilityTypeRepository
    ) {
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSurveyHistoryRepository = userSurveyHistoryRepository;
        this.userVulnerabilityMapRepository = userVulnerabilityMapRepository;
        this.vulnerabilityTypeRepository = vulnerabilityTypeRepository;
    }

    @Transactional(readOnly = true)
    public AdminUserListResponse getUsers() {
        List<AdminUserSummaryResponse> users = userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getUserId))
                .map(this::toSummary)
                .toList();

        return new AdminUserListResponse(users.size(), users);
    }

    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        List<UserSurveyHistory> histories = userSurveyHistoryRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        LocalDateTime latestSurveyAt = histories.isEmpty() ? null : histories.get(0).getCreatedAt();

        return new AdminUserDetailResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                profile != null ? profile.getSkillLevel() : null,
                profile != null && profile.getVulnerabilityType() != null
                        ? profile.getVulnerabilityType().getVulnerabilityTypeId()
                        : null,
                getVulnerabilityTypeIds(userId),
                getVulnerabilityDetails(userId),
                histories.size(),
                latestSurveyAt,
                user.getCreatedAt(),
                profile != null && profile.getUpdatedAt() != null ? profile.getUpdatedAt() : user.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public AdminSurveyHistoryResponse getUserSurveyHistory(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }

        List<AdminSurveyHistoryItemResponse> histories = userSurveyHistoryRepository
                .findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toHistoryItem)
                .toList();

        return new AdminSurveyHistoryResponse(userId, histories);
    }

    private AdminUserSummaryResponse toSummary(User user) {
        Long userId = user.getUserId();
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        List<UserSurveyHistory> histories = userSurveyHistoryRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        LocalDateTime latestSurveyAt = histories.isEmpty() ? null : histories.get(0).getCreatedAt();

        return new AdminUserSummaryResponse(
                userId,
                user.getEmail(),
                user.getNickname(),
                profile != null ? profile.getSkillLevel() : null,
                profile != null && profile.getVulnerabilityType() != null
                        ? profile.getVulnerabilityType().getVulnerabilityTypeId()
                        : null,
                getVulnerabilityTypeIds(userId),
                getVulnerabilityDetails(userId),
                histories.size(),
                latestSurveyAt,
                user.getCreatedAt()
        );
    }

    private AdminSurveyHistoryItemResponse toHistoryItem(UserSurveyHistory history) {
        return new AdminSurveyHistoryItemResponse(
                history.getSurveyHistoryId(),
                history.getSurveyVersion(),
                history.getSkillLevel(),
                readIntegerList(history.getVulnerabilityTypeIdsJson()),
                history.getPrimaryVulnerabilityType() != null
                        ? history.getPrimaryVulnerabilityType().getVulnerabilityTypeId()
                        : null,
                readAnswerMap(history.getAnswersJson()),
                history.getCreatedAt()
        );
    }

    private List<Integer> getVulnerabilityTypeIds(Long userId) {
        return userVulnerabilityMapRepository.findByUserIdOrderByVulnerabilityTypeIdAsc(userId)
                .stream()
                .map(UserVulnerabilityMap::getVulnerabilityTypeId)
                .toList();
    }

    private List<VulnerabilityTypeDetail> getVulnerabilityDetails(Long userId) {
        List<Integer> vulnerabilityTypeIds = getVulnerabilityTypeIds(userId);
        if (vulnerabilityTypeIds.isEmpty()) {
            return List.of();
        }

        return vulnerabilityTypeRepository
                .findByVulnerabilityTypeIdInOrderByVulnerabilityTypeIdAsc(vulnerabilityTypeIds)
                .stream()
                .map(this::toDetail)
                .toList();
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

    private List<Integer> readIntegerList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private Map<String, Integer> readAnswerMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return Map.of();
        }
    }
}
