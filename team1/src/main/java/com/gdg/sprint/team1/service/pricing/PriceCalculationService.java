package com.gdg.sprint.team1.service.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gdg.sprint.team1.dto.pricing.CouponInfo;
import com.gdg.sprint.team1.dto.pricing.PriceCalculationResult;
import com.gdg.sprint.team1.dto.pricing.PriceItem;

@Service
public class PriceCalculationService {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(30000);
    private static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(3000);
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    public PriceCalculationResult calculateTotal(List<PriceItem> items, CouponInfo coupon) {
        if (items == null || items.isEmpty()) {
            return new PriceCalculationResult(ZERO, ZERO, ZERO, ZERO);
        }

        BigDecimal totalProductPrice = items.stream()
            .map(item -> toMoney(item.unitPrice()).multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(ZERO, BigDecimal::add);

        BigDecimal discountAmount = calculateDiscount(totalProductPrice, coupon);
        BigDecimal deliveryFee = calculateDeliveryFee(totalProductPrice);
        BigDecimal finalPrice = totalProductPrice.subtract(discountAmount).add(deliveryFee);

        return new PriceCalculationResult(
            totalProductPrice,
            discountAmount,
            deliveryFee,
            finalPrice
        );
    }

    private BigDecimal calculateDiscount(BigDecimal totalProductPrice, CouponInfo coupon) {
        if (coupon == null || coupon.type() == null) return ZERO;

        BigDecimal minOrderPrice = coupon.minOrderPrice() == null
            ? BigDecimal.ZERO
            : coupon.minOrderPrice();
        if (totalProductPrice.compareTo(minOrderPrice) < 0) return ZERO;

        BigDecimal discountValue = coupon.discountValue() == null
            ? BigDecimal.ZERO
            : coupon.discountValue();

        BigDecimal discount = switch (coupon.type()) {
            case PERCENTAGE -> totalProductPrice
                .multiply(discountValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            case FIXED -> discountValue;
        };

        discount = toMoney(discount);
        if (discount.compareTo(totalProductPrice) > 0) {
            discount = totalProductPrice;
        }
        return discount;
    }

    private BigDecimal calculateDeliveryFee(BigDecimal totalProductPrice) {
        if (totalProductPrice.compareTo(BigDecimal.ZERO) == 0) {
            return ZERO;
        }
        return totalProductPrice.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
            ? ZERO
            : DELIVERY_FEE;
    }

    private BigDecimal toMoney(BigDecimal value) {
        if (value == null) return ZERO;
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
