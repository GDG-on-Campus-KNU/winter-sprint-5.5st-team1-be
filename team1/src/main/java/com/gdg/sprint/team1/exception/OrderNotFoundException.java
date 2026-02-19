package com.gdg.sprint.team1.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Integer orderId) {
        super("존재하지 않는 주문입니다. id=" + orderId);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}
