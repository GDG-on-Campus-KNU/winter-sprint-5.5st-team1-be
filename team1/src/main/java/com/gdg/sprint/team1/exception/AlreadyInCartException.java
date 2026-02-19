package com.gdg.sprint.team1.exception;

public class AlreadyInCartException extends RuntimeException {

    public AlreadyInCartException() {
        super("이미 장바구니에 있는 상품입니다. 수량을 변경하시려면 수정 API를 사용해주세요.");
    }

    public AlreadyInCartException(String message) {
        super(message);
    }
}
