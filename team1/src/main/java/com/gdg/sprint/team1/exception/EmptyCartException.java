package com.gdg.sprint.team1.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("장바구니가 비어있습니다.");
    }

    public EmptyCartException(String message) {
        super(message);
    }
}
