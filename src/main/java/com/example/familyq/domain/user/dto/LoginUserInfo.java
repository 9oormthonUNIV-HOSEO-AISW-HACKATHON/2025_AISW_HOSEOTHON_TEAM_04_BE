package com.example.familyq.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginUserInfo {
    private Long id; // 유저 테이블의 유저의 고유 ID
    private String username;
    private String role;
    private String message; // 반환 메시지
}
