package com.gdg.sprint.team1.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;

@Entity
@Table(name = "user_coupons")
public class UserCoupon {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Getter
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Getter
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Getter
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    protected UserCoupon() {}

    public void use() {
        this.usedAt = LocalDateTime.now();
    }

    public void restore() {
        this.usedAt = null;
    }

    public boolean isUsable() {
        LocalDateTime now = LocalDateTime.now();
        return usedAt == null && expiredAt.isAfter(now);
    }
}
