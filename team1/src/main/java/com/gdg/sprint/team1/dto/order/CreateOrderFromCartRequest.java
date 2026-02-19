package com.gdg.sprint.team1.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 기반 주문 생성 요청")
public record CreateOrderFromCartRequest(

    @Schema(
        description = "사용할 쿠폰 ID (UserCoupons 테이블의 ID)",
        example = "5",
        nullable = true
    )
    Integer userCouponId,

    @Schema(
        description = "수령인 이름",
        example = "홍길동",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 1,
        maxLength = 100
    )
    @NotBlank(message = "수령인 이름은 필수입니다.")
    @Size(max = 100, message = "수령인 이름은 최대 100자까지 입력 가능합니다.")
    String recipientName,

    @Schema(
        description = "수령인 전화번호 (형식: 010-1234-5678)",
        example = "010-1234-5678",
        requiredMode = Schema.RequiredMode.REQUIRED,
        pattern = "^\\d{2,3}-\\d{3,4}-\\d{4}$"
    )
    @NotBlank(message = "수령인 전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    String recipientPhone,

    @Schema(
        description = "배송 주소 (기본 주소)",
        example = "서울특별시 강남구 테헤란로 123",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 500
    )
    @NotBlank(message = "배송 주소는 필수입니다.")
    @Size(max = 500, message = "배송 주소는 최대 500자까지 입력 가능합니다.")
    String deliveryAddress,

    @Schema(
        description = "상세 주소 (동/호수 등)",
        example = "456호",
        nullable = true,
        maxLength = 200
    )
    @Size(max = 200, message = "상세 주소는 최대 200자까지 입력 가능합니다.")
    String deliveryDetailAddress,

    @Schema(
        description = "배송 메시지 (요청사항)",
        example = "문 앞에 놓아주세요",
        nullable = true,
        maxLength = 500
    )
    @Size(max = 500, message = "배송 메시지는 최대 500자까지 입력 가능합니다.")
    String deliveryMessage
) {}