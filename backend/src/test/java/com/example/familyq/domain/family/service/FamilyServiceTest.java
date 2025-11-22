package com.example.familyq.domain.family.service;

import com.example.familyq.domain.family.dto.FamilyCreateResponse;
import com.example.familyq.domain.family.dto.FamilyJoinRequest;
import com.example.familyq.domain.family.dto.FamilyResponse;
import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.family.repository.FamilyRepository;
import com.example.familyq.domain.user.entity.RoleType;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.domain.user.repository.UserRepository;
import com.example.familyq.support.IntegrationTestSupport;
import com.example.familyq.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyServiceTest extends IntegrationTestSupport {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Test
    void 가족_생성시_랜덤_코드가_부여되고_유저가_연결된다() {
        User user = userRepository.save(TestDataFactory.user("family_creator", RoleType.MOTHER, 1985));

        FamilyCreateResponse response = familyService.createFamily(user.getId());

        assertThat(response.getFamilyCode()).hasSizeBetween(6, 8);
        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getFamily()).isNotNull();
        assertThat(updated.getFamily().getId()).isEqualTo(response.getFamilyId());
    }

    @Test
    void 가족_코드로_참여하면_해당_가족으로_연결된다() {
        Family family = familyRepository.save(TestDataFactory.family("JOIN01"));
        User user = userRepository.save(TestDataFactory.user("joiner", RoleType.CHILD, 2012));

        FamilyJoinRequest request = new FamilyJoinRequest();
        request.setFamilyCode(family.getFamilyCode());
        FamilyResponse response = familyService.joinFamily(user.getId(), request);

        assertThat(response.getFamilyId()).isEqualTo(family.getId());
        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getFamily().getId()).isEqualTo(family.getId());
    }
}
