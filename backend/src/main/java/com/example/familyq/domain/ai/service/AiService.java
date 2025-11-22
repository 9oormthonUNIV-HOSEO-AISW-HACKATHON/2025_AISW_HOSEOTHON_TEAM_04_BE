package com.example.familyq.domain.ai.service;

import com.example.familyq.domain.ai.client.FamilyCounselorClient;
import com.example.familyq.domain.ai.dto.CounselingRequest;
import com.example.familyq.domain.ai.dto.CounselingResponse;
import com.example.familyq.domain.question.entity.Answer;
import com.example.familyq.domain.question.entity.FamilyQuestion;
import com.example.familyq.domain.question.repository.AnswerRepository;
import com.example.familyq.domain.question.repository.FamilyQuestionRepository;
import com.example.familyq.domain.user.entity.RoleType;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final FamilyCounselorClient familyCounselorClient;
    private final FamilyQuestionRepository familyQuestionRepository;
    private final AnswerRepository answerRepository;

    private static final String SYSTEM_PROMPT =
            "You are a professional family counselor. When the user provides responses from multiple family members (e.g., two parents and children) about a single topic, your task is to analyze their perspectives, identify emotional states and conflicts, and offer constructive communication guidance. Always return your answer strictly in the following JSON format: { \"content\": \"...analysis and counseling guidance...\" } Do not include any additional fields or text outside this JSON structure.";

    /**
     * 가족 질문 ID로 상담 요청 (모든 답변이 완료된 경우만)
     * @param familyQuestionId 가족 질문 ID
     * @return AI가 생성한 상담 내용
     */
    @Transactional(readOnly = true)
    public String getCounselingByFamilyQuestion(Long familyQuestionId) {
        try {
            log.debug("가족 상담 요청 시작: familyQuestionId={}", familyQuestionId);

            // 가족 질문 조회
            FamilyQuestion familyQuestion = familyQuestionRepository.findById(familyQuestionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_QUESTION_NOT_FOUND));

            // 완료 상태 확인
            if (!familyQuestion.isCompleted()) {
                throw new BusinessException(ErrorCode.FAMILY_QUESTION_NOT_FOUND, "모든 가족 구성원의 답변이 완료되지 않았습니다.");
            }

            // 질문 텍스트 가져오기
            String questionText = familyQuestion.getQuestion().getText();

            // 답변들 조회 (생성 시간 순으로 정렬)
            List<Answer> answers = answerRepository.findByFamilyQuestion(familyQuestion)
                    .stream()
                    .sorted((a1, a2) -> a1.getCreatedAt().compareTo(a2.getCreatedAt()))
                    .collect(Collectors.toList());

            if (answers.isEmpty()) {
                throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND, "답변이 없습니다.");
            }

            // "역할:답변" 형태로 문자열 조합
            String userContent = buildUserContent(questionText, answers);

            // AI API 호출
            String content = callAiApi(userContent);

            log.debug("가족 상담 요청 완료: content 길이={}", content.length());
            return content;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("가족 상담 요청 처리 중 오류 발생", e);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "가족 상담 요청 처리에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 질문과 답변들을 조합하여 사용자 입력 문자열 생성
     * 자녀가 여러 명이면 child1, child2 등으로 구분
     */
    private String buildUserContent(String questionText, List<Answer> answers) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"question\": \"").append(escapeJson(questionText)).append("\", ");

        List<String> roleAnswers = new ArrayList<>();
        int childIndex = 1;

        for (Answer answer : answers) {
            RoleType roleType = answer.getUser().getRoleType();
            String roleName = getRoleName(roleType, childIndex);
            String answerContent = escapeJson(answer.getContent());
            
            roleAnswers.add("\"" + roleName + "\": \"" + answerContent + "\"");
            
            // 자녀인 경우 인덱스 증가
            if (roleType == RoleType.CHILD) {
                childIndex++;
            }
        }

        sb.append(String.join(", ", roleAnswers));
        sb.append(" }");

        return sb.toString();
    }

    /**
     * RoleType을 역할명으로 변환
     * 자녀가 여러 명이면 child1, child2 등으로 구분
     */
    private String getRoleName(RoleType roleType, int childIndex) {
        return switch (roleType) {
            case FATHER -> "father";
            case MOTHER -> "mother";
            case CHILD -> "child" + childIndex;
        };
    }

    /**
     * JSON 문자열에서 특수문자 이스케이프 처리
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * AI API 호출
     */
    private String callAiApi(String userContent) {
        CounselingRequest.Message systemMessage = CounselingRequest.Message.builder()
                .role("system")
                .content(SYSTEM_PROMPT)
                .build();

        CounselingRequest.Message userMessage = CounselingRequest.Message.builder()
                .role("user")
                .content(userContent)
                .build();

        CounselingRequest request = CounselingRequest.builder()
                .model("gpt-5-chat-latest")
                .messages(List.of(systemMessage, userMessage))
                .build();

        CounselingResponse response = familyCounselorClient.requestCounseling(request);
        String content = response.getContent();

        if (content == null || content.isEmpty()) {
            log.warn("AI 응답에서 content가 비어있음");
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI 응답이 비어있습니다.");
        }

        return content;
    }
}

