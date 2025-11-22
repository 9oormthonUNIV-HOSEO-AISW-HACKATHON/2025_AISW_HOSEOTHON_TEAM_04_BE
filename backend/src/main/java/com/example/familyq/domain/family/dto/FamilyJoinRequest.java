package com.example.familyq.domain.family.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FamilyJoinRequest {

    @NotBlank(message = "가족 코드를 입력해 주세요.")
    private String familyCode;
}
