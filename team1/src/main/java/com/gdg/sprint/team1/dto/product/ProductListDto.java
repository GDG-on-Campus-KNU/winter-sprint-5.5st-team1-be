package com.gdg.sprint.team1.dto.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductListDto(
    Long id,
    Long storeId,
    String storeName,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    String productStatus,
    Instant createdAt,
    Instant updatedAt
) {}
