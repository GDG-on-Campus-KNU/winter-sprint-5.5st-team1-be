package com.gdg.sprint.team1.dto.cart;

import java.math.BigDecimal;

public record CartSummary (
        Integer totalItems,
        Integer totalQuantity,
        BigDecimal totalProductPrice,
        BigDecimal deliveryFee,
        BigDecimal finalPrice
) {}
