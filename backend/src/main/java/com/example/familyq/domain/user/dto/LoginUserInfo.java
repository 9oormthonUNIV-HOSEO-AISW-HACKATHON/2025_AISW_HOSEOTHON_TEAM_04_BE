package com.example.familyq.domain.user.dto;

import com.example.familyq.domain.user.entity.RoleType;
import com.example.familyq.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserInfo {

    private Long userId;
    private String loginId;
    private String name;
    private Integer birthYear;
    private RoleType roleType;
    private Long familyId;
    private boolean admin;

    public static LoginUserInfo from(User user) {
        return LoginUserInfo.builder()
                .userId(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .birthYear(user.getBirthYear())
                .roleType(user.getRoleType())
                .familyId(user.getFamily() != null ? user.getFamily().getId() : null)
                .admin(Boolean.TRUE.equals(user.getIsAdmin()))
                .build();
    }

    public boolean hasFamily() {
        return familyId != null;
    }

}
