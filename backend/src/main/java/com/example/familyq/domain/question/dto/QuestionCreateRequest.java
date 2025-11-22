package com.example.familyq.domain.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionCreateRequest {

    @NotBlank(message = "질문 내용을 입력해 주세요.")
    private String text;

    @Positive(message = "순번은 1 이상의 숫자여야 합니다.")
    private Integer orderIndex;
}
