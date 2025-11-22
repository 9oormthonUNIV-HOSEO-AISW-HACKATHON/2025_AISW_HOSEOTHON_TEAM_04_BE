package com.example.familyq.domain.insight.service;

import com.example.familyq.domain.insight.client.AiClient;
import com.example.familyq.domain.insight.dto.InsightGenerateRequest;
import com.example.familyq.domain.insight.dto.InsightResponse;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public InsightResponse generateInsight(InsightGenerateRequest request) {
        return aiClient.generateInsight(request);
    }

    public String serialize(InsightResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "인사이트 직렬화에 실패했습니다.");
        }
    }

    public InsightResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, InsightResponse.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "인사이트 파싱에 실패했습니다.");
        }
    }
}
