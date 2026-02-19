package com.gdg.sprint.team1.controller.api;

import org.springframework.http.ResponseEntity;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.auth.LoginRequest;
import com.gdg.sprint.team1.dto.auth.LoginResponse;
import com.gdg.sprint.team1.dto.auth.RefreshRequest;
import com.gdg.sprint.team1.dto.auth.SignupRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "인증 API", description = "회원가입, 로그인, 토큰 재발급")
public interface AuthApi {

    @Operation(summary = "회원가입", description = "이메일 중복 검증, 비밀번호 BCrypt 해싱, 기본 Role = USER")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 완료")
    })
    ResponseEntity<ApiResponse<Void>> signup(@Valid SignupRequest request);

    @Operation(summary = "로그인", description = "이메일/비밀번호 검증 후 Access Token(30분) + Refresh Token(7일) 발급")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공")
    })
    ResponseEntity<ApiResponse<LoginResponse>> login(@Valid LoginRequest request);

    @Operation(summary = "Access Token 재발급", description = "Refresh Token으로 새 Access Token + 새 Refresh Token 발급 (기존 Refresh Token 무효화)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공")
    })
    ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid RefreshRequest request);
}
