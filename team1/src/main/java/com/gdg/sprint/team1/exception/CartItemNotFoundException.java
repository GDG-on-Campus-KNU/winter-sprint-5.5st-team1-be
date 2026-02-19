package com.gdg.sprint.team1.exception;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException() {
        super("장바구니에 해당 상품이 없습니다.");
    }

    public CartItemNotFoundException(Long productId) {
        super("장바구니에 해당 상품이 없습니다. (상품 ID: " + productId + ")");
    }

    public CartItemNotFoundException(String message) {
        super(message);
    }
}
