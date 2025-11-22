package com.example.familyq.domain.ai.controller;

import com.example.familyq.domain.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI", description = "AI 가족 상담 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    @Operation(summary = "가족 상담 요청", description = "특정 가족 질문의 모든 답변이 완료된 경우, 답변들을 분석하여 상담 내용을 제공합니다.")
    @PostMapping("/counseling/{familyQuestionId}")
    public ResponseEntity<CounselingResponse> requestCounseling(
            @PathVariable Long familyQuestionId) {
        String content = aiService.getCounselingByFamilyQuestion(familyQuestionId);
        return ResponseEntity.ok(new CounselingResponse(content));
    }

    /**
     * 응답 DTO
     */
    public record CounselingResponse(String content) {
    }
}

