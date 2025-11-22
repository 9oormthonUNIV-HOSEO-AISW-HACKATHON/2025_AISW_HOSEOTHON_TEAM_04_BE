package com.example.familyq.domain.family.service;

import com.example.familyq.domain.family.dto.FamilyCreateResponse;
import com.example.familyq.domain.family.dto.FamilyJoinRequest;
import com.example.familyq.domain.family.dto.FamilyResponse;
import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.family.repository.FamilyRepository;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.domain.user.repository.UserRepository;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private static final String FAMILY_CODE_CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;

    @Transactional
    public FamilyCreateResponse createFamily(Long userId) {
        User user = getUser(userId);
        if (user.getFamily() != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_HAS_FAMILY);
        }

        String code = generateUniqueFamilyCode();
        Family family = Family.builder()
                .familyCode(code)
                .build();
        family.addMember(user);
        Family savedFamily = familyRepository.save(family);

        return FamilyCreateResponse.builder()
                .familyId(savedFamily.getId())
                .familyCode(savedFamily.getFamilyCode())
                .build();
    }

    @Transactional
    public FamilyResponse joinFamily(Long userId, FamilyJoinRequest request) {
        User user = getUser(userId);
        if (user.getFamily() != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_HAS_FAMILY);
        }

        Family family = familyRepository.findByFamilyCode(request.getFamilyCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_CODE_INVALID));
        family.addMember(user);

        Family loadedFamily = familyRepository.findWithMembersById(family.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_NOT_FOUND));
        return FamilyResponse.from(loadedFamily);
    }

    @Transactional(readOnly = true)
    public FamilyResponse getMyFamily(Long userId) {
        User user = userRepository.findWithFamilyById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Family family = user.getFamily();
        if (family == null) {
            throw new BusinessException(ErrorCode.USER_NOT_IN_FAMILY);
        }

        Family loadedFamily = familyRepository.findWithMembersById(family.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_NOT_FOUND));
        return FamilyResponse.from(loadedFamily);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String generateUniqueFamilyCode() {
        String code;
        do {
            code = randomCode(ThreadLocalRandom.current().nextInt(6, 9));
        } while (familyRepository.existsByFamilyCode(code));
        return code;
    }

    private String randomCode(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(FAMILY_CODE_CHARSET.length());
            builder.append(FAMILY_CODE_CHARSET.charAt(index));
        }
        return builder.toString();
    }
}
