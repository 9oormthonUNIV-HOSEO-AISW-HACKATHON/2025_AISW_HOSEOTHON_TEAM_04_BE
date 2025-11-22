package com.example.familyq.domain.question.controller;

import com.example.familyq.domain.question.dto.QuestionAdminResponse;
import com.example.familyq.domain.question.dto.QuestionCreateRequest;
import com.example.familyq.domain.question.service.QuestionAdminService;
import com.example.familyq.domain.user.dto.LoginUserInfo;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "질문 관리 (관리자)", description = "관리자 전용 질문 등록, 삭제, 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/questions")
public class QuestionAdminController {

    private final QuestionAdminService questionAdminService;

    @Operation(summary = "질문 목록 조회", description = "시스템에 등록된 모든 질문을 조회합니다. (관리자 전용)")
    @SecurityRequirement(name = "SESSION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 가능)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<List<QuestionAdminResponse>> getQuestions(
            @Parameter(hidden = true) @LoginUser LoginUserInfo loginUser) {
        validateAdmin(loginUser);
        return ResponseEntity.ok(questionAdminService.getQuestions());
    }

    @Operation(summary = "질문 등록", description = "새로운 질문을 시스템에 등록합니다. (관리자 전용)")
    @SecurityRequirement(name = "SESSION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 가능)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<QuestionAdminResponse> createQuestion(
            @Parameter(hidden = true) @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody QuestionCreateRequest request) {
        validateAdmin(loginUser);
        return ResponseEntity.ok(questionAdminService.createQuestion(request));
    }

    @Operation(summary = "질문 삭제", description = "등록된 질문을 삭제합니다. (관리자 전용)")
    @SecurityRequirement(name = "SESSION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 가능)"),
            @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @Parameter(hidden = true) @LoginUser LoginUserInfo loginUser,
            @Parameter(description = "질문 ID") @PathVariable Long questionId) {
        validateAdmin(loginUser);
        questionAdminService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    private void validateAdmin(LoginUserInfo loginUser) {
        if (!loginUser.isAdmin()) {
            throw new BusinessException(ErrorCode.ADMIN_ONLY);
        }
    }
}
