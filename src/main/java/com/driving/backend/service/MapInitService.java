package com.driving.backend.service;

import com.driving.backend.dto.map.MapInitLevelRule;
import com.driving.backend.dto.map.MapInitResponse;
import com.driving.backend.dto.map.MapInitUser;
import com.driving.backend.dto.map.MapInitVulnerabilityType;
import com.driving.backend.dto.map.MapSegmentItem;
import com.driving.backend.entity.LevelRule;
import com.driving.backend.entity.User;
import com.driving.backend.entity.UserProfile;
import com.driving.backend.entity.UserVulnerabilityMap;
import com.driving.backend.entity.VulnerabilityType;
import com.driving.backend.exception.InvalidRequestException;
import com.driving.backend.exception.InvalidTokenException;
import com.driving.backend.exception.UserNotFoundException;
import com.driving.backend.repository.LevelRuleRepository;
import com.driving.backend.repository.UserProfileRepository;
import com.driving.backend.repository.UserRepository;
import com.driving.backend.repository.UserVulnerabilityMapRepository;
import com.driving.backend.repository.VulnerabilityTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MapInitService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserVulnerabilityMapRepository userVulnerabilityMapRepository;
    private final VulnerabilityTypeRepository vulnerabilityTypeRepository;
    private final LevelRuleRepository levelRuleRepository;
    private final MapSegmentService mapSegmentService;

    public MapInitService(
            JwtTokenService jwtTokenService,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserVulnerabilityMapRepository userVulnerabilityMapRepository,
            VulnerabilityTypeRepository vulnerabilityTypeRepository,
            LevelRuleRepository levelRuleRepository,
            MapSegmentService mapSegmentService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userVulnerabilityMapRepository = userVulnerabilityMapRepository;
        this.vulnerabilityTypeRepository = vulnerabilityTypeRepository;
        this.levelRuleRepository = levelRuleRepository;
        this.mapSegmentService = mapSegmentService;
    }

    public MapInitResponse init(
            String authorizationHeader,
            String minLat,
            String minLon,
            String maxLat,
            String maxLon,
            String includeSegments,
            String levels
    ) {
        validateBounds(minLat, minLon, maxLat, maxLon);

        Long userId = extractUserId(authorizationHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User profile not found"));

        List<Integer> userVulnerabilityTypeIds = userVulnerabilityMapRepository
                .findByUserIdOrderByVulnerabilityTypeIdAsc(userId)
                .stream()
                .map(UserVulnerabilityMap::getVulnerabilityTypeId)
                .toList();

        LevelRule activeRule = levelRuleRepository.findFirstByIsActiveTrueOrderByLevelRuleIdDesc().orElse(null);
        List<MapInitVulnerabilityType> vulnerabilityTypes = vulnerabilityTypeRepository
                .findAllByOrderByVulnerabilityTypeIdAsc()
                .stream()
                .map(this::toVulnerabilityType)
                .toList();

        List<MapSegmentItem> segments = parseIncludeSegments(includeSegments)
                ? mapSegmentService.getSegments(minLat, minLon, maxLat, maxLon, levels, null, null).items()
                : List.of();

        return new MapInitResponse(
                new MapInitUser(
                        user.getUserId(),
                        user.getNickname(),
                        profile.getSkillLevel(),
                        profile.getVulnerabilityType() != null ? profile.getVulnerabilityType().getVulnerabilityTypeId() : null
                ),
                userVulnerabilityTypeIds,
                toLevelRule(activeRule),
                buildColorScheme(),
                vulnerabilityTypes,
                segments
        );
    }

    private void validateBounds(String minLatRaw, String minLonRaw, String maxLatRaw, String maxLonRaw) {
        double minLat;
        double minLon;
        double maxLat;
        double maxLon;

        try {
            minLat = Double.parseDouble(minLatRaw);
            minLon = Double.parseDouble(minLonRaw);
            maxLat = Double.parseDouble(maxLatRaw);
            maxLon = Double.parseDouble(maxLonRaw);
        } catch (Exception ex) {
            throw new InvalidRequestException("Invalid bounds");
        }

        if (Double.compare(minLat, maxLat) >= 0 || Double.compare(minLon, maxLon) >= 0) {
            throw new InvalidRequestException("Invalid bounds");
        }
    }

    private boolean parseIncludeSegments(String includeSegments) {
        if (!StringUtils.hasText(includeSegments)) {
            return true;
        }
        return Boolean.parseBoolean(includeSegments);
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

    private MapInitLevelRule toLevelRule(LevelRule rule) {
        if (rule == null) {
            return null;
        }

        return new MapInitLevelRule(
                rule.getLevelRuleId(),
                rule.getVersion(),
                rule.getName(),
                rule.getLevel1Max(),
                rule.getLevel2Max()
        );
    }

    private MapInitVulnerabilityType toVulnerabilityType(VulnerabilityType type) {
        return new MapInitVulnerabilityType(
                type.getVulnerabilityTypeId(),
                type.getCode(),
                type.getName(),
                type.getDescription(),
                type.getIconKey()
        );
    }

    private Map<String, String> buildColorScheme() {
        Map<String, String> colorScheme = new LinkedHashMap<>();
        colorScheme.put("1", "#2ECC71");
        colorScheme.put("2", "#F1C40F");
        colorScheme.put("3", "#E74C3C");
        return colorScheme;
    }
}
