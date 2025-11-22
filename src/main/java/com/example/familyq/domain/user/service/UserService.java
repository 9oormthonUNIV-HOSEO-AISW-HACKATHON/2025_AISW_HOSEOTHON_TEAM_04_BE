package com.example.familyq.domain.user.service;

import com.example.familyq.domain.user.dto.ChangePasswordRequest;
import com.example.familyq.domain.user.dto.SignupRequest;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public User findByName(String nickname) { // 닉네임으로 유저 검색
        return userRepository.findByName(nickname)
                .orElseThrow(() -> new IllegalArgumentException());
    }

    public User findById(Long id) { // User.Id로 검색
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException());
    }


    public User findByUserLoginId(String userLoginId) {
        return userRepository.findByUserId(userLoginId)
                .orElseThrow(() -> new IllegalArgumentException());
    }

    public User findByRole(String role) {
        return userRepository.findByRole(role)
                .orElseThrow(() -> new IllegalArgumentException());
    }


    public Long save(SignupRequest dto) {
        // 유저 정보 저장
        User saved =  userRepository.save(User.builder()
                .userId(dto.userId())
                .password(bCryptPasswordEncoder.encode(dto.password()))
                .name(dto.name())
                .role(dto.role())
                .year(dto.year())
                .month(dto.month())
                .day(dto.day())
                .build()
        );

        return  saved.getId();
    }


    @Transactional
    // 유저 정보 업데이트
    public User changePassword(Long id, ChangePasswordRequest dto) {
        User user = findById(id);

        // 기존 비밀번호 확인
        if (!bCryptPasswordEncoder.matches(dto.oldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호를 암호화하여 저장
        String encodedNewPassword = bCryptPasswordEncoder.encode(dto.newPassword());
        user.setPassword(encodedNewPassword);

        return user;
    }
}
