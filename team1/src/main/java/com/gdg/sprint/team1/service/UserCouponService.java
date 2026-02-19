package com.gdg.sprint.team1.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.dto.order.CouponContext;
import com.gdg.sprint.team1.dto.pricing.CouponInfo;
import com.gdg.sprint.team1.dto.pricing.CouponType;
import com.gdg.sprint.team1.dto.pricing.PriceItem;
import com.gdg.sprint.team1.entity.Coupon;
import com.gdg.sprint.team1.entity.UserCoupon;
import com.gdg.sprint.team1.exception.CouponNotFoundException;
import com.gdg.sprint.team1.exception.InvalidCouponException;
import com.gdg.sprint.team1.exception.MinimumOrderNotMetException;
import com.gdg.sprint.team1.repository.UserCouponRepository;

@Service
@RequiredArgsConstructor
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;

    @Transactional(readOnly = true)
    public CouponContext resolveForOrder(Integer userId, Integer userCouponId, List<PriceItem> priceItems) {
        if (userCouponId == null) {
            return new CouponContext(null, null);
        }

        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new CouponNotFoundException(userCouponId));

        if (!userCoupon.isUsable()) {
            throw new InvalidCouponException("사용할 수 없는 쿠폰입니다.");
        }
        if (!userCoupon.getUser().getId().equals(userId)) {
            throw new InvalidCouponException("본인의 쿠폰만 사용할 수 있습니다.");
        }

        Coupon coupon = userCoupon.getCoupon();
        BigDecimal totalProductPrice = priceItems.stream()
            .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (coupon.getMinOrderPrice() != null
            && totalProductPrice.compareTo(coupon.getMinOrderPrice()) < 0) {
            throw new MinimumOrderNotMetException(totalProductPrice, coupon.getMinOrderPrice());
        }

        CouponInfo couponInfo = new CouponInfo(
            CouponType.valueOf(coupon.getCouponType().name()),
            coupon.getDiscountValue(),
            coupon.getMinOrderPrice()
        );
        return new CouponContext(userCoupon, couponInfo);
    }

    @Transactional(readOnly = true)
    public List<UserCoupon> findCouponsByUserId(Integer userId, String status) {
        List<UserCoupon> list = userCouponRepository.findByUser_IdOrderByIssuedAtDesc(userId);
        Stream<UserCoupon> stream = list.stream();
        if ("AVAILABLE".equalsIgnoreCase(status)) {
            stream = stream.filter(UserCoupon::isUsable);
        } else if ("USED".equalsIgnoreCase(status)) {
            stream = stream.filter(uc -> uc.getUsedAt() != null);
        }
        return stream.toList();
    }
}
