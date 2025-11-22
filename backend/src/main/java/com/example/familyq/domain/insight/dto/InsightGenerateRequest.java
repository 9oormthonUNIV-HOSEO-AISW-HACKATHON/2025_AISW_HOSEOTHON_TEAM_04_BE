package com.example.familyq.domain.insight.dto;

import com.example.familyq.domain.user.entity.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightGenerateRequest {

    private String questionText;
    private List<FamilyMemberContext> familyMembers;
    private List<AnswerContext> answers;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FamilyMemberContext {
        private String name;
        private RoleType roleType;
        private Integer birthYear;
        private String ageGroupTag;
        private String birthOrderTag;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerContext {
        private String userName;
        private RoleType roleType;
        private String ageGroupTag;
        private String birthOrderTag;
        private String content;
    }
}
