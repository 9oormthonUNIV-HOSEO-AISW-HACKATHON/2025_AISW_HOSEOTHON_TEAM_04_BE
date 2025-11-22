package com.example.familyq.domain.family.controller;

import com.example.familyq.domain.family.dto.FamilyCreateResponse;
import com.example.familyq.domain.family.dto.FamilyJoinRequest;
import com.example.familyq.domain.family.dto.FamilyResponse;
import com.example.familyq.domain.family.service.FamilyService;
import com.example.familyq.domain.user.dto.LoginUserInfo;
import com.example.familyq.domain.user.service.UserService;
import com.example.familyq.global.security.LoginUser;
import com.example.familyq.global.security.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가족 관리", description = "가족 생성, 참여, 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/families")
public class FamilyController {

    private final FamilyService familyService;
    private final UserService userService;

    @Operation(summary = "가족 생성", description = "새로운 가족을 생성하고 초대 코드를 발급받습니다.")
    @SecurityRequirement(name = "SESSION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "가족 생성 성공"),
            @ApiResponse(responseCode = "400", description = "이미 가족에 속해 있음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<FamilyCreateResponse> createFamily(
            @Parameter(hidden = true) @LoginUser LoginUserInfo loginUser,
            @Parameter(hidden = true) HttpSession session) {
        FamilyCreateResponse response = familyService.createFamily(loginUser.getUserId());
        refreshSession(session, loginUser.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "가족 참여", description = "초대 코드를 사용하여 기존 가족에 참여합니다.")
    @SecurityRequirement(name = "SESSION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "가족 참여 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 초대 코드 또는 이미 가족에 속해 있음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "해당 코드의 가족을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/join")
    public ResponseEntity<FamilyResponse> joinFamily(
            @Parameter(hidden = true) @LoginUser LoginUserInfo loginUser,
            @Valid @RequestBody FamilyJoinRequest request,
            @Parameter(hidden = true) HttpSession session) {
        FamilyResponse response = familyService.joinFamily(loginUser.getUserId(), request);
        refreshSession(session, loginUser.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 가족 조회", description = "현재 사용자가 속한 가족 정보를 조회합니다.")
    @SecurityRequirement(name = "SESSION")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "가족에 속해있지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/me")
    public ResponseEntity<FamilyResponse> getMyFamily(
            @Parameter(hidden = true) @LoginUser LoginUserInfo loginUser) {
        return ResponseEntity.ok(familyService.getMyFamily(loginUser.getUserId()));
    }

    private void refreshSession(HttpSession session, Long userId) {
        LoginUserInfo updated = LoginUserInfo.from(userService.getUser(userId));
        session.setAttribute(SessionConst.LOGIN_USER, updated);
    }
}
