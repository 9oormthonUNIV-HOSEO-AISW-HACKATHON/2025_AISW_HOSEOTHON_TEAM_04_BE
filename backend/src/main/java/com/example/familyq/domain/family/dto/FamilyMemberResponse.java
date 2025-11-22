package com.example.familyq.domain.family.dto;

import com.example.familyq.domain.user.entity.RoleType;
import com.example.familyq.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FamilyMemberResponse {

    private final Long userId;
    private final String name;
    private final RoleType roleType;
    private final Integer birthYear;

    public static FamilyMemberResponse from(User user) {
        return FamilyMemberResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .roleType(user.getRoleType())
                .birthYear(user.getBirthYear())
                .build();
    }
}
