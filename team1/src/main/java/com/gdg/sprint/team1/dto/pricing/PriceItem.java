package com.gdg.sprint.team1.dto.pricing;

import java.math.BigDecimal;

public record PriceItem(
    Long productId,
    BigDecimal unitPrice,
    int quantity
) {}
