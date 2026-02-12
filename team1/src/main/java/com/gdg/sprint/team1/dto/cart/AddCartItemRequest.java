package com.gdg.sprint.team1.dto.cart;

public record AddCartItemRequest (
    Integer productId,
    Integer quantity
) {}
