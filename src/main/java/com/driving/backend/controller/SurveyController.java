package com.driving.backend.controller;

import com.driving.backend.dto.user.SurveyQuestionsResponse;
import com.driving.backend.service.UserSurveyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/survey")
public class SurveyController {

    private final UserSurveyService userSurveyService;

    public SurveyController(UserSurveyService userSurveyService) {
        this.userSurveyService = userSurveyService;
    }

    @GetMapping("/questions")
    public ResponseEntity<SurveyQuestionsResponse> getSurveyQuestions() {
        return ResponseEntity.ok(userSurveyService.getSurveyQuestions());
    }
}
