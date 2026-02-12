package com.gdg.sprint.team1.dto.cart;

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
