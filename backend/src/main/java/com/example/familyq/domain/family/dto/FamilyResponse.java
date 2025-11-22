package com.example.familyq.domain.family.dto;

import com.example.familyq.domain.family.entity.Family;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class FamilyResponse {

    private final Long familyId;
    private final String familyCode;
    private final List<FamilyMemberResponse> members;

    public static FamilyResponse from(Family family) {
        return FamilyResponse.builder()
                .familyId(family.getId())
                .familyCode(family.getFamilyCode())
                .members(
                        family.getMembers()
                                .stream()
                                .map(FamilyMemberResponse::from)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
