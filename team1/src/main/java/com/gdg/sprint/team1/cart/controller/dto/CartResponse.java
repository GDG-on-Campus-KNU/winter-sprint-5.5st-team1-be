package com.gdg.sprint.team1.cart.controller.dto;

import java.util.List;

public record CartResponse (
    Integer userId,
    List<CartItemResponse> items,
    CartSummary summary
) {}