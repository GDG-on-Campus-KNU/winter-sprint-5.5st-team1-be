package com.gdg.sprint.team1.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.gdg.sprint.team1.entity.Order;

@Schema(description = "주문 목록 조회 응답 (요약 정보)")
public record OrderResponse(

    @Schema(description = "주문 ID", example = "101")
    Integer id,

    @Schema(description = "주문 상태", example = "PENDING",
        allowableValues = {"PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"})
    String orderStatus,

    @Schema(description = "총 상품 금액 (할인 전)", example = "28000.00")
    BigDecimal totalProductPrice,

    @Schema(description = "할인 금액", example = "3000.00")
    BigDecimal discountAmount,

    @Schema(description = "배송비", example = "3000.00")
    BigDecimal deliveryFee,

    @Schema(description = "최종 결제 금액", example = "28000.00")
    BigDecimal finalPrice,

    @Schema(description = "배송 주소", example = "서울특별시 강남구 테헤란로 123")
    String deliveryAddress,

    @Schema(description = "주문 생성 시각", example = "2024-02-08T11:00:00")
    LocalDateTime createdAt,

    @Schema(description = "주문 상품 개수", example = "2")
    Integer itemCount
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderStatus().name(),
            order.getTotalProductPrice(),
            order.getDiscountAmount(),
            order.getDeliveryFee(),
            order.getFinalPrice(),
            order.getDeliveryAddress(),
            order.getCreatedAt(),
            order.getOrderItems().size()
        );
    }
}