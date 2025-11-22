package com.example.familyq.domain.question.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnswerRequest {

    @NotBlank(message = "답변 내용을 입력해 주세요.")
    private String content;
}
