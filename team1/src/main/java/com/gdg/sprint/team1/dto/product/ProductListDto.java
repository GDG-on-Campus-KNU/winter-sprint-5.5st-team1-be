package com.gdg.sprint.team1.dto.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductListDto(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    String productStatus,
    String imageUrl,
    Instant createdAt,
    Instant updatedAt
) {}
