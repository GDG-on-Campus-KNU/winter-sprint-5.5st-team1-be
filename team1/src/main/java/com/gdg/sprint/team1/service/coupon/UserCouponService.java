package com.gdg.sprint.team1.service.coupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.entity.Coupon;
import com.gdg.sprint.team1.entity.UserCoupon;
import com.gdg.sprint.team1.exception.CouponNotFoundException;
import com.gdg.sprint.team1.exception.InvalidCouponException;
import com.gdg.sprint.team1.exception.MinimumOrderNotMetException;
import com.gdg.sprint.team1.repository.UserCouponRepository;
import com.gdg.sprint.team1.dto.order.CouponContext;
import com.gdg.sprint.team1.dto.pricing.CouponInfo;
import com.gdg.sprint.team1.dto.pricing.CouponType;
import com.gdg.sprint.team1.dto.pricing.PriceItem;

/**
 * 사용자 쿠폰(UserCoupon) 관련 서비스.
 * 주문 시 쿠폰 적용 가능 여부 검증 및 CouponInfo 생성을 담당한다.
 */
@Service
@RequiredArgsConstructor
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;

    /**
     * 주문에 적용할 쿠폰을 검증하고, 금액 계산용 CouponInfo까지 포함한 CouponContext를 반환한다.
     * userCouponId가 null이면 쿠폰 미적용으로 CouponContext(null, null)을 반환한다.
     */
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

    /**
     * 사용자의 쿠폰 목록 조회 (마이페이지 등).
     * @param status "AVAILABLE"(사용 가능), "USED"(사용 완료), null(전체)
     */
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
