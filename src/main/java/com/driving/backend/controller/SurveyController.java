package com.driving.backend.controller;

import com.driving.backend.dto.user.SurveyQuestionsResponse;
import com.driving.backend.service.UserSurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/survey")
@Tag(name = "Survey", description = "설문 문항 조회 API")
public class SurveyController {

    private final UserSurveyService userSurveyService;

    public SurveyController(UserSurveyService userSurveyService) {
        this.userSurveyService = userSurveyService;
    }

    @GetMapping("/questions")
    @Operation(summary = "설문 문항 조회", description = "유저 프로필 설문에 필요한 질문 목록을 반환합니다.")
    public ResponseEntity<SurveyQuestionsResponse> getSurveyQuestions() {
        return ResponseEntity.ok(userSurveyService.getSurveyQuestions());
    }
}
