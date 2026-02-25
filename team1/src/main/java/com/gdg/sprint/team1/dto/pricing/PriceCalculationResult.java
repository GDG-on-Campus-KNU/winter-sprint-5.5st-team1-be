package com.gdg.sprint.team1.dto.pricing;

import java.math.BigDecimal;

public record PriceCalculationResult(
    BigDecimal totalProductPrice,
    BigDecimal discountAmount,
    BigDecimal deliveryFee,
    BigDecimal finalPrice
) {}
