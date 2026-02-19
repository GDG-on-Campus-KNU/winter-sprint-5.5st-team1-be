package com.gdg.sprint.team1.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.auth.LoginRequest;
import com.gdg.sprint.team1.dto.auth.LoginResponse;
import com.gdg.sprint.team1.dto.auth.RefreshRequest;
import com.gdg.sprint.team1.dto.auth.SignupRequest;
import com.gdg.sprint.team1.service.auth.AuthService;

import jakarta.validation.Valid;

@Tag(name = "인증 API", description = "회원가입, 로그인")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일 중복 검증, 비밀번호 BCrypt 해싱, 기본 Role = USER")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(null, "회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호 검증 후 Access Token(30분) + Refresh Token(7일) 발급")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "로그인 성공"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Access Token 재발급", description = "Refresh Token으로 새 Access Token + 새 Refresh Token 발급 (기존 Refresh Token은 무효화)")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        LoginResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response, "토큰 재발급 성공"));
    }
}
