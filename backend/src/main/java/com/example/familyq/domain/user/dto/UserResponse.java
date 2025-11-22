package com.example.familyq.domain.user.dto;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.user.entity.RoleType;
import com.example.familyq.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private final Long id;
    private final String loginId;
    private final String name;
    private final Integer birthYear;
    private final RoleType roleType;
    private final Long familyId;
    private final String familyCode;
    private final boolean admin;

    public static UserResponse from(User user) {
        Family family = user.getFamily();
        return UserResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .birthYear(user.getBirthYear())
                .roleType(user.getRoleType())
                .familyId(family != null ? family.getId() : null)
                .familyCode(family != null ? family.getFamilyCode() : null)
                .admin(Boolean.TRUE.equals(user.getIsAdmin()))
                .build();
    }
}
