package com.gdg.sprint.team1.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.gdg.sprint.team1.entity.Order;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 생성 응답")
public record CreateOrderResponse(

    @Schema(description = "주문 요약 정보")
    OrderSummary order,

    @Schema(description = "주문 상품 목록")
    List<OrderItemResponse> orderItems,

    @Schema(description = "적용된 쿠폰 정보 (없으면 null)", nullable = true)
    CouponResponse couponApplied
) {

    @Schema(description = "주문 요약 정보")
    public record OrderSummary(

        @Schema(description = "주문 ID", example = "101")
        Integer id,

        @Schema(description = "사용자 ID", example = "1")
        Integer userId,

        @Schema(description = "사용된 쿠폰 ID (UserCoupons 테이블)", example = "5", nullable = true)
        Integer userCouponId,

        @Schema(description = "주문 상태", example = "PENDING",
            allowableValues = {"PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"})
        String orderStatus,

        @Schema(description = "배송 주소", example = "서울특별시 강남구 테헤란로 123")
        String deliveryAddress,

        @Schema(description = "총 상품 금액 (할인 전)", example = "28000.00")
        BigDecimal totalProductPrice,

        @Schema(description = "할인 금액 (쿠폰)", example = "3000.00")
        BigDecimal discountAmount,

        @Schema(description = "배송비", example = "3000.00")
        BigDecimal deliveryFee,

        @Schema(description = "최종 결제 금액", example = "28000.00")
        BigDecimal finalPrice,

        @Schema(description = "주문 생성 시각", example = "2024-02-08T11:00:00")
        LocalDateTime createdAt,

        @Schema(description = "주문 수정 시각", example = "2024-02-08T11:00:00")
        LocalDateTime updatedAt
    ) {}

    public static CreateOrderResponse from(Order order) {
        OrderSummary orderSummary = new OrderSummary(
            order.getId(),
            order.getUser().getId(),
            order.getUserCoupon() != null ? order.getUserCoupon().getId() : null,
            order.getOrderStatus().name(),
            order.getDeliveryAddress(),
            order.getTotalProductPrice(),
            order.getDiscountAmount(),
            order.getDeliveryFee(),
            order.getFinalPrice(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );

        List<OrderItemResponse> items = order.getOrderItems().stream()
            .map(OrderItemResponse::from)
            .toList();

        CouponResponse couponResponse = null;
        if (order.getUserCoupon() != null) {
            couponResponse = CouponResponse.from(order.getUserCoupon(), order.getDiscountAmount());
        }

        return new CreateOrderResponse(orderSummary, items, couponResponse);
    }
}