package com.gdg.sprint.team1.cart.controller.dto;

public record CartSummary (
        Integer totalItems,
        Integer totalQuantity,
        Integer totalProductPrice,
        Integer deliveryFee,
        Integer finalPrice
) {}
