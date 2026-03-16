package com.driving.backend.controller;

import com.driving.backend.dto.user.SubmitSurveyRequest;
import com.driving.backend.dto.user.SubmitSurveyResponse;
import com.driving.backend.dto.user.UpdateNicknameRequest;
import com.driving.backend.dto.user.UpdateProfileResponse;
import com.driving.backend.dto.user.UserProfileResponse;
import com.driving.backend.service.UserProfileService;
import com.driving.backend.service.UserSurveyService;
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
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserSurveyService userSurveyService;

    public UserProfileController(UserProfileService userProfileService, UserSurveyService userSurveyService) {
        this.userProfileService = userProfileService;
        this.userSurveyService = userSurveyService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getMe(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @GetMapping("/survey")
    public ResponseEntity<UserProfileResponse> getMySurveySummary(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @GetMapping("/vulnerabilities")
    public ResponseEntity<UserProfileResponse> getMyVulnerabilities(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @PatchMapping("/profile")
    public ResponseEntity<UpdateProfileResponse> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) UpdateNicknameRequest request
    ) {
        return ResponseEntity.ok(userProfileService.updateNickname(authorizationHeader, request));
    }

    @PostMapping("/survey")
    public ResponseEntity<SubmitSurveyResponse> submitSurvey(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) SubmitSurveyRequest request
    ) {
        return ResponseEntity.ok(userSurveyService.submitSurvey(authorizationHeader, request));
    }
}
