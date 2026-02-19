package com.gdg.sprint.team1.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

/**
 * 주문 엔티티
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id")
    private UserCoupon userCoupon;

    @Column(name = "total_product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalProductPrice;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    @Builder.Default  //주문은 항상 PENDING으로 시작
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "delivery_detail_address", columnDefinition = "TEXT")
    private String deliveryDetailAddress;

    @Column(name = "delivery_message", columnDefinition = "TEXT")
    private String deliveryMessage;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

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

    public void cancel(String reason) {
        if (this.orderStatus != OrderStatus.PENDING &&
            this.orderStatus != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                "취소할 수 없는 주문 상태입니다: " + this.orderStatus
            );
        }
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelReason = reason;
    }

    public boolean canCancel() {
        return this.orderStatus == OrderStatus.PENDING
            || this.orderStatus == OrderStatus.CONFIRMED;
    }

    public enum OrderStatus {
        PENDING,    // 주문 대기
        CONFIRMED,  // 주문 확인
        SHIPPING,   // 배송 중
        DELIVERED,  // 배송 완료
        CANCELLED   // 주문 취소
    }
}