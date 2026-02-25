package com.gdg.sprint.team1.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인/리프레시 응답 (Access Token + Refresh Token)")
public record LoginResponse(

    @Schema(description = "JWT Access Token (Bearer 헤더에 사용)")
    String accessToken,

    @Schema(description = "Refresh Token (Access 재발급 시 사용)")
    String refreshToken,

    @Schema(description = "토큰 타입", example = "Bearer")
    String tokenType
) {
    public static LoginResponse of(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, refreshToken, "Bearer");
    }
}
