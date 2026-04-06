package com.driving.backend.controller;

import com.driving.backend.dto.user.SurveyQuestionsResponse;
import com.driving.backend.service.UserSurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/survey")
@Tag(name = "Survey", description = "설문 문항 및 설문 결과 관련 API")
public class SurveyController {

    private final UserSurveyService userSurveyService;

    public SurveyController(UserSurveyService userSurveyService) {
        this.userSurveyService = userSurveyService;
    }

    @Operation(summary = "설문 문항 조회", description = "설문 페이지에서 사용할 문항, 선택지, 역채점 여부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SurveyQuestionsResponse.class)))
    })
    @GetMapping("/questions")
    public ResponseEntity<SurveyQuestionsResponse> getSurveyQuestions() {
        return ResponseEntity.ok(userSurveyService.getSurveyQuestions());
    }
}
