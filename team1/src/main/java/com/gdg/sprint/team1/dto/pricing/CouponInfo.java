package com.gdg.sprint.team1.dto.pricing;

import java.math.BigDecimal;

public record CouponInfo(
    CouponType type,
    BigDecimal discountValue,
    BigDecimal minOrderPrice
) {}
