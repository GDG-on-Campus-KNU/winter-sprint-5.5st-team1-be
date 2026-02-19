package com.gdg.sprint.team1.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh Token 재발급 요청")
public record RefreshRequest(

    @NotBlank(message = "Refresh Token은 필수입니다.")
    @Schema(description = "로그인 시 발급받은 Refresh Token", requiredMode = Schema.RequiredMode.REQUIRED)
    String refreshToken
) {}
