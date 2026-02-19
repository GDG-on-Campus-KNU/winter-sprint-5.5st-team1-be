package com.gdg.sprint.team1.dto.order;

import com.gdg.sprint.team1.dto.pricing.CouponInfo;
import com.gdg.sprint.team1.entity.UserCoupon;

public record CouponContext(UserCoupon userCoupon, CouponInfo couponInfo) {}
