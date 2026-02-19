package com.gdg.sprint.team1.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.gdg.sprint.team1.entity.Order;

@Schema(description = "주문 취소 응답")
public record CancelOrderResponse(

    @Schema(description = "주문 ID", example = "101")
    Integer orderId,

    @Schema(description = "주문 상태 (CANCELLED 고정)", example = "CANCELLED")
    String orderStatus,

    @Schema(description = "취소 사유", example = "단순 변심", nullable = true)
    String cancelReason,

    @Schema(description = "취소 완료 시각", example = "2024-02-08T11:10:00")
    LocalDateTime cancelledAt,

    @Schema(description = "환불 금액 (최종 결제 금액)", example = "28000.00")
    BigDecimal refundAmount
) {
    public static CancelOrderResponse from(Order order) {
        return new CancelOrderResponse(
            order.getId(),
            order.getOrderStatus().name(),
            order.getCancelReason(),
            order.getUpdatedAt(),
            order.getFinalPrice()
        );
    }
}