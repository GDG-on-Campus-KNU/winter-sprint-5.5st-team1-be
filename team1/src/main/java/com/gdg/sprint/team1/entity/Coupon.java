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

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "min_order_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrderPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false, length = 50)
    private CouponType couponType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "coupon_status", nullable = false, length = 50)
    private String couponStatus = "ACTIVE";

    @Column(name = "valid_days", nullable = false)
    private Integer validDays = 30;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMinOrderPrice() { return minOrderPrice; }
    public void setMinOrderPrice(BigDecimal minOrderPrice) { this.minOrderPrice = minOrderPrice; }
    public CouponType getCouponType() { return couponType; }
    public void setCouponType(CouponType couponType) { this.couponType = couponType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCouponStatus() { return couponStatus; }
    public void setCouponStatus(String couponStatus) { this.couponStatus = couponStatus; }
    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
