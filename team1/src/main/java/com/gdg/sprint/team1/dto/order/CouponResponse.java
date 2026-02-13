package com.gdg.sprint.team1.dto.order;

import java.math.BigDecimal;

import com.gdg.sprint.team1.entity.UserCoupon;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "적용된 쿠폰 정보")
public record CouponResponse(

    @Schema(description = "사용자 보유 쿠폰 ID (UserCoupons 테이블)", example = "5")
    Integer userCouponId,

    @Schema(description = "쿠폰 마스터 ID (Coupons 테이블)", example = "2")
    Integer couponId,

    @Schema(description = "쿠폰 이름", example = "신규가입 할인")
    String couponName,

    @Schema(description = "쿠폰 타입", example = "FIXED",
        allowableValues = {"PERCENTAGE", "FIXED"})
    String couponType,

    @Schema(description = "할인 값 (PERCENTAGE면 %, FIXED면 원)", example = "3000.00")
    BigDecimal discountValue,

    @Schema(description = "실제 할인된 금액 (원)", example = "3000.00")
    BigDecimal discountAmount
) {
    public static CouponResponse from(UserCoupon userCoupon, BigDecimal discountAmount) {
        return new CouponResponse(
            userCoupon.getId(),
            userCoupon.getCoupon().getId(),
            userCoupon.getCoupon().getName(),
            userCoupon.getCoupon().getCouponType().name(),
            userCoupon.getCoupon().getDiscountValue(),
            discountAmount
        );
    }
}