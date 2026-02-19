package com.gdg.sprint.team1.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, Integer requested, Integer available) {
        super(String.format("%s의 재고가 부족합니다. (요청: %d개, 재고: %d개)", 
            productName, requested, available));
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}
