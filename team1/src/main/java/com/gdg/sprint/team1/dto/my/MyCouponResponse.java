package com.gdg.sprint.team1.dto.my;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.gdg.sprint.team1.entity.UserCoupon;

@Schema(description = "마이페이지 보유 쿠폰 응답")
public record MyCouponResponse(

    @Schema(description = "사용자 보유 쿠폰 ID")
    Integer userCouponId,

    @Schema(description = "쿠폰 이름")
    String couponName,

    @Schema(description = "최소 주문 금액")
    BigDecimal minOrderPrice,

    @Schema(description = "할인 값 (PERCENTAGE: %, FIXED: 원)")
    BigDecimal discountValue,

    @Schema(description = "쿠폰 타입", allowableValues = {"PERCENTAGE", "FIXED"})
    String couponType,

    @Schema(description = "만료 일시")
    LocalDateTime expiredAt,

    @Schema(description = "사용 일시 (미사용 시 null)")
    LocalDateTime usedAt,

    @Schema(description = "사용 가능 여부 (미사용·미만료)")
    Boolean available
) {
    public static MyCouponResponse from(UserCoupon uc) {
        return new MyCouponResponse(
            uc.getId(),
            uc.getCoupon().getName(),
            uc.getCoupon().getMinOrderPrice(),
            uc.getCoupon().getDiscountValue(),
            uc.getCoupon().getCouponType().name(),
            uc.getExpiredAt(),
            uc.getUsedAt(),
            uc.isUsable()
        );
    }
}
