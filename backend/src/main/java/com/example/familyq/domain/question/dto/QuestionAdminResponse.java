package com.example.familyq.domain.question.dto;

import com.example.familyq.domain.question.entity.Question;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionAdminResponse {

    private final Long id;
    private final String text;
    private final Integer orderIndex;

    public static QuestionAdminResponse from(Question question) {
        return QuestionAdminResponse.builder()
                .id(question.getId())
                .text(question.getText())
                .orderIndex(question.getOrderIndex())
                .build();
    }
}
