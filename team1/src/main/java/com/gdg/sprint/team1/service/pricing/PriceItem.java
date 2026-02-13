package com.gdg.sprint.team1.service.pricing;

import java.math.BigDecimal;

public record PriceItem(
    Long productId,
    BigDecimal unitPrice,
    int quantity
) {}
