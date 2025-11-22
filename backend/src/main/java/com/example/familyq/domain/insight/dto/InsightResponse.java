package com.example.familyq.domain.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {
    private String commonPoints;
    private String differences;
    private List<String> suggestedDialogue;
}

