package com.driving.backend.controller;

import com.driving.backend.dto.admin.AdminSurveyHistoryResponse;
import com.driving.backend.dto.admin.AdminUserDetailResponse;
import com.driving.backend.dto.admin.AdminUserListResponse;
import com.driving.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@Controller
class AdminPageController {

    @GetMapping("/admin")
    RedirectView adminPage() {
        return new RedirectView("/admin.html");
    }
}

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin Users", description = "관리자용 유저 조회 API")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "유저 목록 조회", description = "전체 유저와 현재 프로필/설문 이력 요약을 조회합니다.")
    public ResponseEntity<AdminUserListResponse> getUsers() {
        return ResponseEntity.ok(adminUserService.getUsers());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "유저 상세 조회", description = "특정 유저의 프로필과 취약특성 정보를 조회합니다.")
    public ResponseEntity<AdminUserDetailResponse> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getUser(userId));
    }

    @GetMapping("/{userId}/survey-history")
    @Operation(summary = "유저 설문 이력 조회", description = "특정 유저의 설문 제출 이력을 최신순으로 조회합니다.")
    public ResponseEntity<AdminSurveyHistoryResponse> getUserSurveyHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getUserSurveyHistory(userId));
    }
}
