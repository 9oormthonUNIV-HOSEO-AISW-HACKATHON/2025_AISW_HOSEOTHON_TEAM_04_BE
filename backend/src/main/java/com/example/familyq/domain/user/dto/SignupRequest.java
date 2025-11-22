package com.example.familyq.domain.user.dto;

import com.example.familyq.domain.user.entity.RoleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "아이디는 필수 값입니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 값입니다.")
    private String password;

    @NotBlank(message = "이름은 필수 값입니다.")
    private String name;

    @NotNull(message = "출생연도는 필수 값입니다.")
    @Min(value = 1900, message = "출생연도는 1900년 이후여야 합니다.")
    @Max(value = 2100, message = "출생연도는 4자리 연도여야 합니다.")
    private Integer birthYear;

    @NotNull(message = "역할을 선택해 주세요.")
    private RoleType roleType;
}
