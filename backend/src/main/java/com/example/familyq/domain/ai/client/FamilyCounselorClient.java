package com.example.familyq.domain.ai.client;

import com.example.familyq.domain.ai.dto.CounselingRequest;
import com.example.familyq.domain.ai.dto.CounselingResponse;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class FamilyCounselorClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiKey;

    public FamilyCounselorClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${ai.api.url}") String apiUrl,
            @Value("${ai.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public CounselingResponse requestCounseling(CounselingRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<CounselingRequest> entity = new HttpEntity<>(request, headers);

            log.debug("AI API 요청: URL={}, Model={}", apiUrl, request.getModel());

            ResponseEntity<CounselingResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    CounselingResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("AI API 응답 성공");
                return response.getBody();
            } else {
                log.error("AI API 응답 실패: Status={}", response.getStatusCode());
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI API 응답이 올바르지 않습니다.");
            }

        } catch (RestClientException e) {
            log.error("AI API 호출 중 오류 발생", e);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI API 호출에 실패했습니다: " + e.getMessage());
        }
    }
}

