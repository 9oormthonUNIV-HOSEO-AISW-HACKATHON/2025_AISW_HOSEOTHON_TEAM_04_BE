package com.example.familyq.domain.question.dto;

import com.example.familyq.domain.insight.dto.InsightResponse;
import com.example.familyq.domain.question.entity.FamilyQuestion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QuestionDetailResponse {

    private final Long familyQuestionId;
    private final Integer sequenceNumber;
    private final String questionText;
    private final LocalDate assignedDate;
    private final LocalDateTime completedAt;
    private final boolean completed;
    private final AnswerResponse myAnswer;
    private final List<AnswerResponse> answers;
    private final InsightResponse insight;
    private final String insightJson;

    public static QuestionDetailResponse of(FamilyQuestion familyQuestion,
                                            AnswerResponse myAnswer,
                                            List<AnswerResponse> answers,
                                            InsightResponse insight) {
        return QuestionDetailResponse.builder()
                .familyQuestionId(familyQuestion.getId())
                .sequenceNumber(familyQuestion.getSequenceNumber())
                .questionText(familyQuestion.getQuestion().getText())
                .assignedDate(familyQuestion.getAssignedDate())
                .completedAt(familyQuestion.getCompletedAt())
                .completed(familyQuestion.isCompleted())
                .myAnswer(myAnswer)
                .answers(answers)
                .insight(insight)
                .insightJson(familyQuestion.getInsightJson())
                .build();
    }
}
