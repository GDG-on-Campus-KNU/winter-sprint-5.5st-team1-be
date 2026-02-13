package com.gdg.sprint.team1.exception;

import java.math.BigDecimal;

public class MinimumOrderNotMetException extends RuntimeException {

    private final BigDecimal currentAmount;
    private final BigDecimal minimumRequired;

    public MinimumOrderNotMetException(BigDecimal currentAmount, BigDecimal minimumRequired) {
        super(String.format("최소 주문 금액을 충족하지 못했습니다. (현재: %s원, 최소: %s원)", 
            currentAmount, minimumRequired));
        this.currentAmount = currentAmount;
        this.minimumRequired = minimumRequired;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public BigDecimal getMinimumRequired() {
        return minimumRequired;
    }
}
