package com.example.familyq.domain.family.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FamilyCreateResponse {

    private final Long familyId;
    private final String familyCode;
}
