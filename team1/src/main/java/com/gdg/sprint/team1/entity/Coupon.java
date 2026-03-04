package com.gdg.sprint.team1.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter
    @Column(nullable = false)
    private String name;

    @Getter
    @Column(name = "min_order_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrderPrice = BigDecimal.ZERO;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false, length = 50)
    private CouponType couponType;

    @Getter
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Getter
    @Column(columnDefinition = "TEXT")
    private String description;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_status", nullable = false, length = 20)
    private CouponStatus couponStatus = CouponStatus.ACTIVE;

    @Getter
    @Column(name = "valid_days", nullable = false)
    private Integer validDays = 30;

    @Getter
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Getter
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Coupon() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CouponType {
        PERCENTAGE, FIXED
    }

    public enum CouponStatus {
        ACTIVE,
        INACTIVE
    }
}
