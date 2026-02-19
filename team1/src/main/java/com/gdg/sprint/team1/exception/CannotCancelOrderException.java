package com.gdg.sprint.team1.exception;

import com.gdg.sprint.team1.entity.Order.OrderStatus;

public class CannotCancelOrderException extends RuntimeException {

    private final OrderStatus currentStatus;

    public CannotCancelOrderException(OrderStatus currentStatus) {
        super(getMessageForStatus(currentStatus));
        this.currentStatus = currentStatus;
    }

    private static String getMessageForStatus(OrderStatus status) {
        return switch (status) {
            case SHIPPING -> "배송 중인 주문은 취소할 수 없습니다.";
            case DELIVERED -> "배송 완료된 주문은 취소할 수 없습니다.";
            case CANCELLED -> "이미 취소된 주문입니다.";
            default -> "취소할 수 없는 주문 상태입니다.";
        };
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }
}
