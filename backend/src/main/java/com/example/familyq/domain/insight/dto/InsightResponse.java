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

    private List<String> commonThemes;
    private List<String> generationDifferences;
    private List<String> conversationSuggestions;
}
