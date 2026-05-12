package com.driving.backend.controller;

import com.driving.backend.dto.admin.AdminSurveyHistoryResponse;
import com.driving.backend.dto.admin.AdminUserDetailResponse;
import com.driving.backend.dto.admin.AdminUserListResponse;
import com.driving.backend.exception.InvalidTokenException;
import com.driving.backend.service.AdminUserService;
import com.driving.backend.service.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin Users", description = "관리자용 유저 조회 API (로그인 필수)")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final JwtTokenService jwtTokenService;

    public AdminUserController(AdminUserService adminUserService, JwtTokenService jwtTokenService) {
        this.adminUserService = adminUserService;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Authorization: Bearer <token> 검증 + role=ADMIN 체크.
     * 토큰 없음/유효하지 않음/권한 부족 모두 401.
     */
    private void requireAdmin(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("관리자 페이지는 로그인이 필요합니다.");
        }
        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new InvalidTokenException("관리자 페이지는 로그인이 필요합니다.");
        }
        jwtTokenService.extractUserId(token); // invalid면 throw
        String role = jwtTokenService.extractRole(token);
        if (!"ADMIN".equals(role)) {
            throw new InvalidTokenException("관리자 권한이 필요합니다.");
        }
    }

    @GetMapping
    @Operation(summary = "유저 목록 조회", description = "전체 유저와 현재 프로필/설문 이력 요약을 조회합니다.")
    public ResponseEntity<AdminUserListResponse> getUsers(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        requireAdmin(authorizationHeader);
        return ResponseEntity.ok(adminUserService.getUsers());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "유저 상세 조회", description = "특정 유저의 프로필과 취약특성 정보를 조회합니다.")
    public ResponseEntity<AdminUserDetailResponse> getUser(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long userId) {
        requireAdmin(authorizationHeader);
        return ResponseEntity.ok(adminUserService.getUser(userId));
    }

    @GetMapping("/{userId}/survey-history")
    @Operation(summary = "유저 설문 이력 조회", description = "특정 유저의 설문 제출 이력을 최신순으로 조회합니다.")
    public ResponseEntity<AdminSurveyHistoryResponse> getUserSurveyHistory(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long userId) {
        requireAdmin(authorizationHeader);
        return ResponseEntity.ok(adminUserService.getUserSurveyHistory(userId));
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "유저 role 변경", description = "USER ↔ ADMIN role 변경.")
    public ResponseEntity<AdminUserDetailResponse> updateUserRole(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long userId,
            @RequestBody UpdateRoleRequest request) {
        requireAdmin(authorizationHeader);
        return ResponseEntity.ok(adminUserService.updateRole(userId, request == null ? null : request.role()));
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "유저 정지/활성", description = "active=false로 설정 시 해당 유저는 로그인이 차단됩니다.")
    public ResponseEntity<AdminUserDetailResponse> updateUserStatus(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long userId,
            @RequestBody UpdateStatusRequest request) {
        requireAdmin(authorizationHeader);
        boolean active = request != null && request.active() != null && request.active();
        return ResponseEntity.ok(adminUserService.updateActive(userId, active));
    }

    public record UpdateRoleRequest(String role) {}
    public record UpdateStatusRequest(Boolean active) {}
}
