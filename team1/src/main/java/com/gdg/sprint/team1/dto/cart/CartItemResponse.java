package com.gdg.sprint.team1.dto.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartItemResponse (
        Long productId,
        String productName,
        BigDecimal productPrice,
        String productStatus,
        Integer quantity,
        BigDecimal subtotal,
        Boolean isAvailable,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
