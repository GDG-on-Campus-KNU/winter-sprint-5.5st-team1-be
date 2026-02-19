package com.gdg.sprint.team1.dto.auth;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.gdg.sprint.team1.entity.User;

@Schema(description = "내 정보 응답 (GET /users/me)")
public record UserMeResponse(

    @Schema(description = "사용자 ID")
    Integer id,

    @Schema(description = "이메일")
    String email,

    @Schema(description = "이름")
    String name,

    @Schema(description = "전화번호")
    String phone,

    @Schema(description = "주소")
    String address,

    @Schema(description = "역할", allowableValues = {"USER", "ADMIN"})
    String role,

    @Schema(description = "가입 일시")
    LocalDateTime createdAt
) {
    public static UserMeResponse from(User user) {
        return new UserMeResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getPhone(),
            user.getAddress(),
            user.getRole().name(),
            user.getCreatedAt()
        );
    }
}
