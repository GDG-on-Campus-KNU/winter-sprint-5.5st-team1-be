package com.gdg.sprint.team1.cart.controller.dto;

public record AddCartItemRequest (
    Integer productId,
    Integer quantity
) {}
