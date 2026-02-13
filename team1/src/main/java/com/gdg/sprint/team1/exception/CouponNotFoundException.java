package com.gdg.sprint.team1.exception;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(Integer couponId) {
        super("존재하지 않는 쿠폰입니다. id=" + couponId);
    }

    public CouponNotFoundException(String message) {
        super(message);
    }
}
