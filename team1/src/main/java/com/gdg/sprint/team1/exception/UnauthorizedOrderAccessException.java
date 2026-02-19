package com.gdg.sprint.team1.exception;

public class UnauthorizedOrderAccessException extends RuntimeException {

    public UnauthorizedOrderAccessException() {
        super("본인의 주문만 조회할 수 있습니다.");
    }

    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}
