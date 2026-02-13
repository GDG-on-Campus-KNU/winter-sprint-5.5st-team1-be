package com.gdg.sprint.team1.exception;

public class EmptyOrderException extends RuntimeException {

    public EmptyOrderException() {
        super("주문 항목이 비어있습니다.");
    }

    public EmptyOrderException(String message) {
        super(message);
    }
}
