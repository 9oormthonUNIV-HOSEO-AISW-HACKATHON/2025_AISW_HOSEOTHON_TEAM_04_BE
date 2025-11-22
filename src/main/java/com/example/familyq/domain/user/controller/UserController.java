package com.example.familyq.domain.user.controller;

import com.example.familyq.domain.user.dto.ChangePasswordRequest;
import com.example.familyq.domain.user.dto.LoginUserInfo;
import com.example.familyq.domain.user.dto.UserResponse;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@SessionAttribute(name = "userId", required = false) Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        User user = userService.findById(userId);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 사용자 정보 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 이름으로 조회
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<UserResponse> getUserByName(@PathVariable String name) {
        User user = userService.findByName(name);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 역할로 조회
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<UserResponse> getUserByRole(@PathVariable String role) {
        User user = userService.findByRole(role);
        UserResponse response = UserResponse.from(user);

        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request,
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        
        // 본인만 비밀번호 변경 가능
        if (sessionUserId == null || !sessionUserId.equals(id)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        userService.changePassword(id, request);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }
}

