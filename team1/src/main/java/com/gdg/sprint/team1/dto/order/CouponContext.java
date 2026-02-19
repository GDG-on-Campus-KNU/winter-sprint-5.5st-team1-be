package com.gdg.sprint.team1.dto.order;

import com.gdg.sprint.team1.dto.pricing.CouponInfo;
import com.gdg.sprint.team1.entity.UserCoupon;

/**
 * 주문 생성 시 적용할 쿠폰 정보.
 * userCoupon·couponInfo가 null이면 쿠폰 미적용.
 */
public record CouponContext(UserCoupon userCoupon, CouponInfo couponInfo) {}
