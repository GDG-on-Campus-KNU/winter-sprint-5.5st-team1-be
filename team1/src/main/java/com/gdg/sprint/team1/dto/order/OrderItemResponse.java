package com.gdg.sprint.team1.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.gdg.sprint.team1.entity.OrderItem;

@Schema(description = "주문 상품 아이템 정보")
public record OrderItemResponse(

    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "상품명", example = "A4 노트")
    String productName,

    @Schema(description = "주문 수량", example = "2")
    Integer quantity,

    @Schema(description = "구매 단가", example = "9000.00")
    BigDecimal unitPrice,

    @Schema(description = "소계 (단가 × 수량)", example = "18000.00")
    BigDecimal subtotal,

    @Schema(description = "주문 생성 시각", example = "2024-02-08T11:00:00")
    LocalDateTime createdAt
) {
    public static OrderItemResponse from(OrderItem orderItem) {
        BigDecimal subtotal = orderItem.getUnitPrice()
            .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

        return new OrderItemResponse(
            orderItem.getProduct().getId(),
            orderItem.getProduct().getName(),
            orderItem.getQuantity(),
            orderItem.getUnitPrice(),
            subtotal,
            orderItem.getCreatedAt()
        );
    }
}