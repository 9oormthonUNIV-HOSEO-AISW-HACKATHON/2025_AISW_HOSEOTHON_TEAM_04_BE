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
    private final String insightJson;

    public static DailyQuestionResponse empty() {
        return DailyQuestionResponse.builder()
                .familyQuestionId(null)
                .sequenceNumber(0)
                .questionText("아직 오늘의 질문이 없습니다")
                .assignedDate(null)
                .requiredMemberCount(0)
                .answeredCount(0)
                .completed(false)
                .myAnswer(null)
                .insight(null)
                .insightJson(null)
                .build();
    }

    public static DailyQuestionResponse of(FamilyQuestion familyQuestion,
                                           int answeredCount,
                                           int requiredMemberCount,
                                           AnswerResponse myAnswer,
                                           InsightResponse insight,
                                           String insightJson) {
        return DailyQuestionResponse.builder()
                .familyQuestionId(familyQuestion.getId())
                .sequenceNumber(familyQuestion.getSequenceNumber())
                .questionText(familyQuestion.getQuestion().getText())
                .assignedDate(familyQuestion.getAssignedDate())
                .requiredMemberCount(requiredMemberCount)
                .answeredCount(answeredCount)
                .completed(familyQuestion.isCompleted())
                .myAnswer(myAnswer)
                .insight(insight)
                .insightJson(insightJson)
                .build();
    }
}
