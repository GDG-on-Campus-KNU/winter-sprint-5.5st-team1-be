package com.gdg.sprint.team1.service.pricing;

import java.math.BigDecimal;

public record PriceCalculationResult(
    BigDecimal totalProductPrice,
    BigDecimal discountAmount,
    BigDecimal deliveryFee,
    BigDecimal finalPrice
) {}
