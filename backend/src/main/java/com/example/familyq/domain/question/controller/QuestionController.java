package com.example.familyq.domain.question.controller;

import com.example.familyq.domain.question.dto.AnswerRequest;
import com.example.familyq.domain.question.dto.AnswerResponse;
import com.example.familyq.domain.question.dto.DailyQuestionResponse;
import com.example.familyq.domain.question.dto.QuestionDetailResponse;
import com.example.familyq.domain.question.dto.QuestionHistoryItemResponse;
import com.example.familyq.domain.question.service.AnswerService;
import com.example.familyq.domain.question.service.QuestionService;
import com.example.familyq.domain.user.dto.LoginUserInfo;
import com.example.familyq.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "질문 및 답변", description = "일일 질문 조회, 답변 제출, 히스토리 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerService answerService;

    @Operation(summary = "오늘의 질문 조회", description = "가족에게 할당된 오늘의 질문을 조회합니다.")
    @SecurityRequirement(name = "SESSION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가족이 없거나 오늘의 질문이 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/today")
    public ResponseEntity<DailyQuestionResponse> getTodayQuestion(
            @Parameter(hidden = true) @LoginUser LoginUserInfo loginUser) {
        return ResponseEntity.ok(questionService.getTodayQuestion(loginUser.getUserId()));
    }

    @Operation(summary = "질문 히스토리 조회", description = "가족의 과거 질문 목록을 조회합니다.")
    @SecurityRequirement(name = "SESSION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가족이 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/history")
    public ResponseEntity<List<QuestionHistoryItemResponse>> getHistory(
            @Parameter(hidden = true) @LoginUser LoginUserInfo loginUser) {
        return ResponseEntity.ok(questionService.getHistory(loginUser.getUserId()));
    }

    @Operation(summary = "질문 상세 조회", description = "특정 질문의 상세 정보와 답변들을 조회합니다.")
    @SecurityRequirement(name = "SESSION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 가족의 질문)"),
            @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{familyQuestionId}")
    public ResponseEntity<QuestionDetailResponse> getQuestionDetail(
            @Parameter(hidden = true) @LoginUser LoginUserInfo loginUser,
            @Parameter(description = "가족 질문 ID") @PathVariable Long familyQuestionId) {
        return ResponseEntity.ok(questionService.getQuestionDetail(loginUser.getUserId(), familyQuestionId));
    }

    @Operation(summary = "답변 제출", description = "질문에 대한 답변을 제출합니다.")
    @SecurityRequirement(name = "SESSION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답변 제출 성공"),
            @ApiResponse(responseCode = "400", description = "이미 답변했거나 유효하지 않은 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 가족의 질문)"),
            @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{familyQuestionId}/answers")
    public ResponseEntity<AnswerResponse> submitAnswer(
            @Parameter(hidden = true) @LoginUser LoginUserInfo loginUser,
            @Parameter(description = "가족 질문 ID") @PathVariable Long familyQuestionId,
            @Valid @RequestBody AnswerRequest request) {
        return ResponseEntity.ok(answerService.submitAnswer(loginUser.getUserId(), familyQuestionId, request));
    }
}
