package com.gdg.sprint.team1.dto.my;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 정보 수정 요청 (PATCH /api/v1/my/info)")
public record UpdateMyInfoRequest (
    @Size(max = 100, message = "이름은 최대 100자까지 입력 가능합니다.")
    @Schema(description = "이름(선택)", example = "홍길동")
    String name,

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    @Schema(description = "전화번호(선택)", example = "010-1234-5678")
    String phone,

    @Size(max = 500, message = "주소는 최대 500자까지 입력 가능합니다.")
    @Schema(description = "주소(선택)", example = "서울특별시 강남구 테헤란로 123")
    String address
) {
    public boolean hasAnyField() {
        return hasText(name) || hasText(phone) || hasText(address);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
