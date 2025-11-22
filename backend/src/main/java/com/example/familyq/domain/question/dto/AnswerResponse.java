package com.example.familyq.domain.question.dto;

import com.example.familyq.domain.question.entity.Answer;
import com.example.familyq.domain.user.entity.RoleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnswerResponse {

    private final Long answerId;
    private final Long userId;
    private final String userName;
    private final RoleType roleType;
    private final String content;
    private final boolean mine;
    private final LocalDateTime createdAt;

    public static AnswerResponse of(Answer answer, Long currentUserId) {
        return AnswerResponse.builder()
                .answerId(answer.getId())
                .userId(answer.getUser().getId())
                .userName(answer.getUser().getName())
                .roleType(answer.getUser().getRoleType())
                .content(answer.getContent())
                .mine(answer.getUser().getId().equals(currentUserId))
                .createdAt(answer.getCreatedAt())
                .build();
    }
}
