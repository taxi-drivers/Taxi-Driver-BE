package com.driving.backend.service;

import com.driving.backend.dto.user.SubmitSurveyRequest;
import com.driving.backend.dto.user.SubmitSurveyResponse;
import com.driving.backend.dto.user.SurveyQuestionOptionResponse;
import com.driving.backend.dto.user.SurveyQuestionResponse;
import com.driving.backend.dto.user.SurveyQuestionsResponse;
import com.driving.backend.entity.UserProfile;
import com.driving.backend.entity.UserVulnerabilityMap;
import com.driving.backend.entity.VulnerabilityType;
import com.driving.backend.exception.InvalidRequestException;
import com.driving.backend.exception.InvalidTokenException;
import com.driving.backend.exception.UserNotFoundException;
import com.driving.backend.repository.UserProfileRepository;
import com.driving.backend.repository.UserVulnerabilityMapRepository;
import com.driving.backend.repository.VulnerabilityTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UserSurveyService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<SurveyQuestionOptionResponse> SURVEY_OPTIONS = List.of(
            new SurveyQuestionOptionResponse(1, "전혀 아니다"),
            new SurveyQuestionOptionResponse(2, "그렇지 않다"),
            new SurveyQuestionOptionResponse(3, "보통이다"),
            new SurveyQuestionOptionResponse(4, "그렇다"),
            new SurveyQuestionOptionResponse(5, "매우 그렇다")
    );

    private final JwtTokenService jwtTokenService;
    private final UserProfileRepository userProfileRepository;
    private final UserVulnerabilityMapRepository userVulnerabilityMapRepository;
    private final VulnerabilityTypeRepository vulnerabilityTypeRepository;

    public UserSurveyService(
            JwtTokenService jwtTokenService,
            UserProfileRepository userProfileRepository,
            UserVulnerabilityMapRepository userVulnerabilityMapRepository,
            VulnerabilityTypeRepository vulnerabilityTypeRepository
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userProfileRepository = userProfileRepository;
        this.userVulnerabilityMapRepository = userVulnerabilityMapRepository;
        this.vulnerabilityTypeRepository = vulnerabilityTypeRepository;
    }

    public SurveyQuestionsResponse getSurveyQuestions() {
        List<SurveyQuestionResponse> questions = SurveyScoringSupport.questions().stream()
                .map(question -> new SurveyQuestionResponse(
                        question.code(),
                        question.category(),
                        question.prompt(),
                        question.reverseScored(),
                        SURVEY_OPTIONS
                ))
                .toList();

        return new SurveyQuestionsResponse(SurveyScoringSupport.SURVEY_VERSION, questions);
    }

    @Transactional
    public SubmitSurveyResponse submitSurvey(String authorizationHeader, SubmitSurveyRequest request) {
        Long userId = extractUserId(authorizationHeader);
        SurveySubmissionPayload payload = resolveSubmissionPayload(request);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        profile.setSkillLevel(payload.skillLevel());
        VulnerabilityType primaryType = payload.primaryVulnerabilityTypeId() == null
                ? null
                : vulnerabilityTypeRepository.findById(payload.primaryVulnerabilityTypeId())
                .orElseThrow(() -> new InvalidRequestException("Invalid request body"));
        profile.setVulnerabilityType(primaryType);
        UserProfile savedProfile = userProfileRepository.save(profile);

        userVulnerabilityMapRepository.deleteByUserId(userId);
        List<UserVulnerabilityMap> mappings = payload.vulnerabilityTypeIds().stream()
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
                userId,
                savedProfile.getSkillLevel(),
                payload.vulnerabilityTypeIds(),
                primaryType != null ? primaryType.getVulnerabilityTypeId() : null,
                savedProfile.getUpdatedAt()
        );
    }

    private SurveySubmissionPayload resolveSubmissionPayload(SubmitSurveyRequest request) {
        if (request != null && request.answers() != null && !request.answers().isEmpty()) {
            return buildPayloadFromAnswers(request.answers());
        }

        validateLegacyRequest(request);

        List<Integer> normalizedVulnerabilityIds = normalizeVulnerabilityIds(request.vulnerabilityTypeIds());
        validateVulnerabilityTypeIds(normalizedVulnerabilityIds);
        validatePrimaryVulnerabilityTypeId(request.primaryVulnerabilityTypeId(), normalizedVulnerabilityIds);

        return new SurveySubmissionPayload(
                request.skillLevel(),
                normalizedVulnerabilityIds,
                request.primaryVulnerabilityTypeId()
        );
    }

    private SurveySubmissionPayload buildPayloadFromAnswers(Map<String, Integer> answers) {
        SurveyScoringSupport.SurveyEvaluation evaluation = SurveyScoringSupport.evaluate(answers);
        Map<String, Integer> vulnerabilityIdByCode = loadVulnerabilityIdsByCode(
                evaluation.vulnerabilityCodes(),
                evaluation.primaryVulnerabilityCode()
        );

        List<Integer> vulnerabilityTypeIds = evaluation.vulnerabilityCodes().stream()
                .map(vulnerabilityIdByCode::get)
                .toList();

        Integer primaryVulnerabilityTypeId = evaluation.primaryVulnerabilityCode() == null
                ? null
                : vulnerabilityIdByCode.get(evaluation.primaryVulnerabilityCode());

        return new SurveySubmissionPayload(
                evaluation.skillLevel(),
                vulnerabilityTypeIds,
                primaryVulnerabilityTypeId
        );
    }

    private Map<String, Integer> loadVulnerabilityIdsByCode(List<String> vulnerabilityCodes, String primaryVulnerabilityCode) {
        Set<String> codes = new LinkedHashSet<>(vulnerabilityCodes);
        if (primaryVulnerabilityCode != null) {
            codes.add(primaryVulnerabilityCode);
        }

        Map<String, Integer> resolved = new LinkedHashMap<>();
        for (String code : codes) {
            Integer vulnerabilityTypeId = vulnerabilityTypeRepository.findByCode(code)
                    .map(VulnerabilityType::getVulnerabilityTypeId)
                    .orElseThrow(() -> new InvalidRequestException("Survey vulnerability type is not configured"));
            resolved.put(code, vulnerabilityTypeId);
        }
        return resolved;
    }

    private void validateLegacyRequest(SubmitSurveyRequest request) {
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

    private record SurveySubmissionPayload(
            Integer skillLevel,
            List<Integer> vulnerabilityTypeIds,
            Integer primaryVulnerabilityTypeId
    ) {
    }
}
