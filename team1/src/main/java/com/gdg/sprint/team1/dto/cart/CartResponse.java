package com.gdg.sprint.team1.dto.cart;

import java.util.List;

public record CartResponse (
    Integer userId,
    List<CartItemResponse> items,
    CartSummary summary
) {}