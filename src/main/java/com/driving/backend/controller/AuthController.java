package com.driving.backend.controller;

import com.driving.backend.dto.auth.LoginRequest;
import com.driving.backend.dto.auth.LoginResponse;
import com.driving.backend.dto.auth.LogoutResponse;
import com.driving.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles HTTP requests and maps them to service-layer operations.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "로그인과 로그아웃 API")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "테스트 유저로 로그인하고 JWT 토큰을 발급합니다.")
    public ResponseEntity<LoginResponse> login(@RequestBody(required = false) LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Authorization 헤더의 토큰을 기준으로 로그아웃 처리합니다.")
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(authService.logout(authorizationHeader));
    }
}

