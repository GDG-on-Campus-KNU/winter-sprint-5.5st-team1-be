package com.gdg.sprint.team1.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignupRequest(

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @Schema(description = "비밀번호 (8자 이상)", example = "Password1!", requiredMode = Schema.RequiredMode.REQUIRED)
    String password,

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 100)
    @Schema(description = "이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Size(max = 20)
    @Schema(description = "전화번호", example = "010-1234-5678")
    String phone,

    @Schema(description = "주소")
    String address
) {}
