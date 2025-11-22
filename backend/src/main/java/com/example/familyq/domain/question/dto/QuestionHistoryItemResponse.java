package com.example.familyq.domain.question.dto;

import com.example.familyq.domain.question.entity.FamilyQuestion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class QuestionHistoryItemResponse {

    private final Long familyQuestionId;
    private final Integer sequenceNumber;
    private final String questionText;
    private final LocalDate assignedDate;
    private final LocalDateTime completedAt;
    private final boolean completed;

    public static QuestionHistoryItemResponse from(FamilyQuestion familyQuestion) {
        return QuestionHistoryItemResponse.builder()
                .familyQuestionId(familyQuestion.getId())
                .sequenceNumber(familyQuestion.getSequenceNumber())
                .questionText(familyQuestion.getQuestion().getText())
                .assignedDate(familyQuestion.getAssignedDate())
                .completedAt(familyQuestion.getCompletedAt())
                .completed(familyQuestion.isCompleted())
                .build();
    }
}
