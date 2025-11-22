package com.example.familyq.domain.insight.client;

import com.example.familyq.domain.insight.dto.InsightGenerateRequest;
import com.example.familyq.domain.insight.dto.InsightResponse;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AiClient {

    public InsightResponse generateInsight(InsightGenerateRequest request) {
        try {
            return InsightResponse.builder()
                    .commonThemes(extractCommonThemes(request))
                    .generationDifferences(extractGenerationDifferences(request))
                    .conversationSuggestions(suggestConversations(request))
                    .build();
        } catch (Exception e) {
            log.error("Fail to generate insight", e);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }
    }

    private List<String> extractCommonThemes(InsightGenerateRequest request) {
        String concatenated = request.getAnswers()
                .stream()
                .map(InsightGenerateRequest.AnswerContext::getContent)
                .collect(Collectors.joining(", "));
        return List.of("가족 구성원들이 공유한 생각: " + concatenated);
    }

    private List<String> extractGenerationDifferences(InsightGenerateRequest request) {
        return request.getAnswers()
                .stream()
                .limit(2)
                .map(answer -> answer.getRoleType() + " 시각: " + answer.getContent())
                .collect(Collectors.toList());
    }

    private List<String> suggestConversations(InsightGenerateRequest request) {
        return List.of(
                String.format("'%s'에 대해 서로의 생각을 더 깊게 나눠보세요.", request.getQuestionText()),
                "각자의 감정을 더 구체적으로 표현해 보는 시간을 가져보세요."
        );
    }
}
