package com.driving.backend.controller;

import com.driving.backend.dto.user.SubmitSurveyRequest;
import com.driving.backend.dto.user.SubmitSurveyResponse;
import com.driving.backend.dto.user.UpdateNicknameRequest;
import com.driving.backend.dto.user.UpdateProfileResponse;
import com.driving.backend.dto.user.UserProfileResponse;
import com.driving.backend.service.UserProfileService;
import com.driving.backend.service.UserSurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "User", description = "내 프로필, 설문 결과, 취약특성 관련 API")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserSurveyService userSurveyService;

    public UserProfileController(UserProfileService userProfileService, UserSurveyService userSurveyService) {
        this.userProfileService = userProfileService;
        this.userSurveyService = userSurveyService;
    }

    @Operation(summary = "내 사용자 정보 조회", description = "사이드바/마이페이지에서 사용할 내 프로필과 설문 요약 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class)))
    @GetMapping
    public ResponseEntity<UserProfileResponse> getMe(
            @Parameter(description = "Bearer Access Token", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @Operation(summary = "내 프로필 상세 조회", description = "마이페이지에서 사용할 프로필 상세 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @Parameter(description = "Bearer Access Token", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @Operation(summary = "내 설문 요약 조회", description = "설문 결과 모달에서 사용할 설문 저장 결과를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/survey")
    public ResponseEntity<UserProfileResponse> getMySurveySummary(
            @Parameter(description = "Bearer Access Token", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @Operation(summary = "내 취약특성 조회", description = "플로팅 버튼/필터에서 사용할 사용자 취약특성 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/vulnerabilities")
    public ResponseEntity<UserProfileResponse> getMyVulnerabilities(
            @Parameter(description = "Bearer Access Token", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(userProfileService.getMyProfile(authorizationHeader));
    }

    @Operation(summary = "내 프로필 수정", description = "현재는 닉네임만 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(schema = @Schema(implementation = UpdateProfileResponse.class)))
    @PatchMapping("/profile")
    public ResponseEntity<UpdateProfileResponse> updateMyProfile(
            @Parameter(description = "Bearer Access Token", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) UpdateNicknameRequest request
    ) {
        return ResponseEntity.ok(userProfileService.updateNickname(authorizationHeader, request));
    }

    @Operation(summary = "설문 결과 저장", description = "설문 answers를 보내면 서버가 채점하고, 직접 skill_level과 vulnerability_type_ids를 보내도 저장할 수 있습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공",
                    content = @Content(schema = @Schema(implementation = SubmitSurveyResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 설문 값"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/survey")
    public ResponseEntity<SubmitSurveyResponse> submitSurvey(
            @Parameter(description = "Bearer Access Token", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) SubmitSurveyRequest request
    ) {
        return ResponseEntity.ok(userSurveyService.submitSurvey(authorizationHeader, request));
    }
}
