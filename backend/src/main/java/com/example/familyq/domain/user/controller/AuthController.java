package com.example.familyq.domain.user.controller;

import com.example.familyq.domain.user.dto.LoginRequest;
import com.example.familyq.domain.user.dto.LoginUserInfo;
import com.example.familyq.domain.user.dto.SignupRequest;
import com.example.familyq.domain.user.dto.UserResponse;
import com.example.familyq.domain.user.service.UserService;
import com.example.familyq.global.security.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 관리", description = "회원가입, 로그인, 로그아웃 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복된 이메일 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = userService.signup(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 세션을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 불일치)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpServletRequest) {
        LoginUserInfo loginUser = userService.login(request);
        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(SessionConst.LOGIN_USER, loginUser);

        UserResponse response = userService.getMe(loginUser.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 무효화하고 로그아웃합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // JSESSIONID 쿠키 명시적 삭제
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setPath("/");  // 쿠키가 설정된 경로와 동일하게 설정
        sessionCookie.setMaxAge(0);  // 즉시 만료
        sessionCookie.setHttpOnly(true);  // 보안을 위해 HttpOnly 플래그 설정
        response.addCookie(sessionCookie);

        return ResponseEntity.noContent().build();
    }
}
