package com.gdg.sprint.team1.cart.controller.dto;

import java.time.LocalDateTime;

public record CartItemResponse (
        Integer productId,
        Integer quantity,
        Integer unitPrice,
        Integer subtotal,
        Boolean isAvailable,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
