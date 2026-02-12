package com.gdg.sprint.team1.dto.cart;

public record CartSummary (
        Integer totalItems,
        Integer totalQuantity,
        Integer totalProductPrice,
        Integer deliveryFee,
        Integer finalPrice
) {}
