package com.example.familyq.domain.user.controller;

import com.example.familyq.domain.user.dto.LoginRequest;
import com.example.familyq.domain.user.dto.LoginUserInfo;
import com.example.familyq.domain.user.dto.SignupRequest;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody SignupRequest request) {
        Long userId = userService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginUserInfo> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        // 사용자 조회
        User user = userService.findByUserLoginId(request.getId());

        // 비밀번호 확인
        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 세션 생성
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("userId", user.getId());

        // 응답 생성
        LoginUserInfo loginUserInfo = LoginUserInfo.builder()
                .id(user.getId())
                .username(user.getName())
                .role(user.getRole())
                .message("로그인 성공")
                .build();

        return ResponseEntity.ok(loginUserInfo);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("로그아웃 성공");
    }
}

