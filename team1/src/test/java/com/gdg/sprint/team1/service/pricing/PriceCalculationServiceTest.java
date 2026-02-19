package com.gdg.sprint.team1.service.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class PriceCalculationServiceTest {

    private final PriceCalculationService service = new PriceCalculationService();

    @Test
    void calculateTotal_withoutCoupon_appliesDeliveryFee() {
        List<PriceItem> items = List.of(
            new PriceItem(1L, new BigDecimal("10000.00"), 2)
        );

        PriceCalculationResult result = service.calculateTotal(items, null);

        assertThat(result.totalProductPrice()).isEqualByComparingTo("20000.00");
        assertThat(result.discountAmount()).isEqualByComparingTo("0.00");
        assertThat(result.deliveryFee()).isEqualByComparingTo("3000.00");
        assertThat(result.finalPrice()).isEqualByComparingTo("23000.00");
    }

    @Test
    void calculateTotal_fixedCoupon_appliesDiscount() {
        List<PriceItem> items = List.of(
            new PriceItem(1L, new BigDecimal("10000.00"), 2)
        );
        CouponInfo coupon = new CouponInfo(
            CouponType.FIXED,
            new BigDecimal("2000.00"),
            new BigDecimal("8000.00")
        );

        PriceCalculationResult result = service.calculateTotal(items, coupon);

        assertThat(result.totalProductPrice()).isEqualByComparingTo("20000.00");
        assertThat(result.discountAmount()).isEqualByComparingTo("2000.00");
        assertThat(result.deliveryFee()).isEqualByComparingTo("3000.00");
        assertThat(result.finalPrice()).isEqualByComparingTo("21000.00");
    }

    @Test
    void calculateTotal_percentageCoupon_appliesDiscountAndFreeShipping() {
        List<PriceItem> items = List.of(
            new PriceItem(1L, new BigDecimal("15000.00"), 2)
        );
        CouponInfo coupon = new CouponInfo(
            CouponType.PERCENTAGE,
            new BigDecimal("10.00"),
            new BigDecimal("10000.00")
        );

        PriceCalculationResult result = service.calculateTotal(items, coupon);

        assertThat(result.totalProductPrice()).isEqualByComparingTo("30000.00");
        assertThat(result.discountAmount()).isEqualByComparingTo("3000.00");
        assertThat(result.deliveryFee()).isEqualByComparingTo("0.00");
        assertThat(result.finalPrice()).isEqualByComparingTo("27000.00");
    }
}
