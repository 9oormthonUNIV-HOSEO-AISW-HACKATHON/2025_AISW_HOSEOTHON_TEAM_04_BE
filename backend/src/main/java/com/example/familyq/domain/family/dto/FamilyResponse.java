package com.example.familyq.domain.family.dto;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.question.QuestionPolicy;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class FamilyResponse {

    private final Long familyId;
    private final String familyCode;
    private final List<FamilyMemberResponse> members;
    private final Integer memberCount;
    private final boolean questionsStarted;
    private final boolean readyForQuestions;

    public static FamilyResponse from(Family family) {
        int memberCount = family.getMembers().size();
        return FamilyResponse.builder()
                .familyId(family.getId())
                .familyCode(family.getFamilyCode())
                .memberCount(memberCount)
                .questionsStarted(Boolean.TRUE.equals(family.getQuestionsStarted()))
                .readyForQuestions(memberCount >= QuestionPolicy.MIN_MEMBERS_TO_START)
                .members(
                        family.getMembers()
                                .stream()
                                .map(FamilyMemberResponse::from)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
