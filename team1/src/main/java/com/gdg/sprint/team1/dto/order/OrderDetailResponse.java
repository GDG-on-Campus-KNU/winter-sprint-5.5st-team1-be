package com.gdg.sprint.team1.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;

import com.gdg.sprint.team1.entity.Order;

@Schema(description = "주문 상세 조회 응답")
public record OrderDetailResponse(

    @Schema(description = "주문 상세 정보")
    OrderInfo order,

    @Schema(description = "주문 상품 목록")
    List<OrderItemResponse> orderItems,

    @Schema(description = "사용된 쿠폰 정보 (없으면 null)", nullable = true)
    CouponResponse coupon
) {

    @Schema(description = "주문 상세 정보")
    public record OrderInfo(

        @Schema(description = "주문 ID", example = "101")
        Integer id,

        @Schema(description = "사용자 ID", example = "1")
        Integer userId,

        @Schema(description = "사용된 쿠폰 ID", example = "5", nullable = true)
        Integer userCouponId,

        @Schema(description = "주문 상태", example = "PENDING",
            allowableValues = {"PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"})
        String orderStatus,

        @Schema(description = "수령인 이름", example = "홍길동")
        String recipientName,

        @Schema(description = "수령인 전화번호", example = "010-1234-5678")
        String recipientPhone,

        @Schema(description = "배송 주소 (기본)", example = "서울특별시 강남구 테헤란로 123")
        String deliveryAddress,

        @Schema(description = "배송 상세 주소", example = "456호", nullable = true)
        String deliveryDetailAddress,

        @Schema(description = "배송 메시지", example = "문 앞에 놓아주세요", nullable = true)
        String deliveryMessage,

        @Schema(description = "총 상품 금액 (할인 전)", example = "28000.00")
        BigDecimal totalProductPrice,

        @Schema(description = "할인 금액", example = "3000.00")
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

    public static OrderDetailResponse from(Order order) {
        OrderInfo orderInfo = new OrderInfo(
            order.getId(),
            order.getUser().getId(),
            order.getUserCoupon() != null ? order.getUserCoupon().getId() : null,
            order.getOrderStatus().name(),
            order.getRecipientName(),
            order.getRecipientPhone(),
            order.getDeliveryAddress(),
            order.getDeliveryDetailAddress(),
            order.getDeliveryMessage(),
            order.getTotalProductPrice(),
            order.getDiscountAmount(),
            order.getDeliveryFee(),
            order.getFinalPrice(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );

        List<OrderItemResponse> items = order.getOrderItems().stream()
            .map(OrderItemResponse::from)
            .collect(Collectors.toList());

        CouponResponse couponResponse = null;
        if (order.getUserCoupon() != null) {
            couponResponse = CouponResponse.from(order.getUserCoupon(), order.getDiscountAmount());
        }

        return new OrderDetailResponse(orderInfo, items, couponResponse);
    }
}