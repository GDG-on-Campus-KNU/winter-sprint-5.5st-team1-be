package com.gdg.sprint.team1.dto.my;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gdg.sprint.team1.entity.UserCoupon;

import io.swagger.v3.oas.annotations.media.Schema;

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
    Boolean available,

    @Schema(description = "만료까지 남은 일수 (만료/사용완료 시 0)")
    Long expiresInDays

) {
    public static MyCouponResponse from(UserCoupon uc) {
        boolean usable = uc.isUsable();
        long days = 0L;
        if (usable && uc.getExpiredAt() != null) {
            days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), uc.getExpiredAt().toLocalDate());
            if (days < 0) days = 0L;
        }

        return new MyCouponResponse(
            uc.getId(),
            uc.getCoupon().getName(),
            uc.getCoupon().getMinOrderPrice(),
            uc.getCoupon().getDiscountValue(),
            uc.getCoupon().getCouponType().name(),
            uc.getExpiredAt(),
            uc.getUsedAt(),
            usable,
            days
        );
    }
}
