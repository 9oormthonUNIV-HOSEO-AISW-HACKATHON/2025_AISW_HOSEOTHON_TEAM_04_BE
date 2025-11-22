package com.example.familyq.domain.question.dto;

import com.example.familyq.domain.insight.dto.InsightResponse;
import com.example.familyq.domain.question.entity.FamilyQuestion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyQuestionResponse {

    private final Long familyQuestionId;
    private final Integer sequenceNumber;
    private final String questionText;
    private final LocalDate assignedDate;
    private final Integer requiredMemberCount;
    private final Integer answeredCount;
    private final boolean completed;
    private final AnswerResponse myAnswer;
    private final InsightResponse insight;

    public static DailyQuestionResponse of(FamilyQuestion familyQuestion,
                                           int answeredCount,
                                           AnswerResponse myAnswer,
                                           InsightResponse insight) {
        return DailyQuestionResponse.builder()
                .familyQuestionId(familyQuestion.getId())
                .sequenceNumber(familyQuestion.getSequenceNumber())
                .questionText(familyQuestion.getQuestion().getText())
                .assignedDate(familyQuestion.getAssignedDate())
                .requiredMemberCount(familyQuestion.getRequiredMemberCount())
                .answeredCount(answeredCount)
                .completed(familyQuestion.isCompleted())
                .myAnswer(myAnswer)
                .insight(insight)
                .build();
    }
}
