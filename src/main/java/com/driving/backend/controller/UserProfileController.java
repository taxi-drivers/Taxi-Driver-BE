package com.driving.backend.controller;

import com.driving.backend.dto.user.SubmitSurveyRequest;
import com.driving.backend.dto.user.SubmitSurveyResponse;
import com.driving.backend.dto.user.SurveyHistoryResponse;
import com.driving.backend.dto.user.UpdateNicknameRequest;
import com.driving.backend.dto.user.UpdateProfileResponse;
import com.driving.backend.dto.user.UserProfileResponse;
import com.driving.backend.service.UserProfileService;
import com.driving.backend.service.UserSurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
@Tag(name = "User Profile", description = "내 정보와 설문 결과 API")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserSurveyService userSurveyService;

    public UserProfileController(UserProfileService userProfileService, UserSurveyService userSurveyService) {
        this.userProfileService = userProfileService;
        this.userSurveyService = userSurveyService;
    }

    @GetMapping
    @Operation(summary = "내 정보 조회", description = "현재 로그인 사용자의 프로필과 취약 특성 정보를 조회합니다.")
    public ResponseEntity<UserProfileResponse> getMe(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @GetMapping("/profile")
    @Operation(summary = "내 프로필 조회", description = "현재 로그인 사용자의 프로필을 조회합니다.")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @GetMapping("/survey")
    @Operation(summary = "내 설문 요약 조회", description = "현재 로그인 사용자의 설문 기반 프로필 요약을 조회합니다.")
    public ResponseEntity<UserProfileResponse> getMySurveySummary(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @GetMapping("/survey/history")
    @Operation(summary = "Survey history", description = "Return the current user's survey submission history.")
    public ResponseEntity<SurveyHistoryResponse> getMySurveyHistory(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userSurveyService.getMySurveyHistory(authorizationHeader));
    }

    @GetMapping("/vulnerabilities")
    @Operation(summary = "내 취약요소 조회", description = "현재 로그인 사용자의 취약 특성 정보를 조회합니다.")
    public ResponseEntity<UserProfileResponse> getMyVulnerabilities(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @PatchMapping("/profile")
    @Operation(summary = "닉네임 수정", description = "현재 로그인 사용자의 닉네임을 수정합니다.")
    public ResponseEntity<UpdateProfileResponse> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) UpdateNicknameRequest request
    ) {
        return ResponseEntity.ok(userProfileService.updateNickname(authorizationHeader, request));
    }

    @PostMapping("/survey")
    @Operation(summary = "설문 제출", description = "설문 응답을 제출하고 사용자 프로필을 갱신합니다.")
    public ResponseEntity<SubmitSurveyResponse> submitSurvey(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) SubmitSurveyRequest request
    ) {
        return ResponseEntity.ok(userSurveyService.submitSurvey(authorizationHeader, request));
    }
}
