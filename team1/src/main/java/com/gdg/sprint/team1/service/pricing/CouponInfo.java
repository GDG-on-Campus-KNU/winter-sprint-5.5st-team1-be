package com.gdg.sprint.team1.service.pricing;

import java.math.BigDecimal;

public record CouponInfo(
    CouponType type,
    BigDecimal discountValue,
    BigDecimal minOrderPrice
) {}
