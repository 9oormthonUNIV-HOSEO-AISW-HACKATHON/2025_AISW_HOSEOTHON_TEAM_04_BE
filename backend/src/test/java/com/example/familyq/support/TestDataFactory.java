package com.example.familyq.support;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.user.entity.RoleType;
import com.example.familyq.domain.user.entity.User;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static User user(String loginId, RoleType roleType, int birthYear) {
        return User.builder()
                .loginId(loginId)
                .passwordHash("encoded")
                .name(loginId + "-name")
                .birthYear(birthYear)
                .roleType(roleType)
                .build();
    }

    public static Family family(String code) {
        return Family.builder()
                .familyCode(code)
                .build();
    }
}
