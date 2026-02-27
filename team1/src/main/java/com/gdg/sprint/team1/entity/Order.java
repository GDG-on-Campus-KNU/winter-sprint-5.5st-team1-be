package com.gdg.sprint.team1.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;

@Entity
@Table(name = "orders")
public class Order {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id")
    private UserCoupon userCoupon;

    @Getter
    @Column(name = "total_product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalProductPrice;

    @Getter
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Getter
    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Getter
    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Getter
    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Getter
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Getter
    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Getter
    @Column(name = "delivery_detail_address")
    private String deliveryDetailAddress;

    @Getter
    @Column(name = "delivery_message", length = 500)
    private String deliveryMessage;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Getter
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Getter
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Getter
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Getter
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Order() {}

    public static Order create(
        User user,
        UserCoupon userCoupon,
        BigDecimal totalProductPrice,
        BigDecimal discountAmount,
        BigDecimal deliveryFee,
        BigDecimal finalPrice,
        String recipientName,
        String recipientPhone,
        String deliveryAddress,
        String deliveryDetailAddress,
        String deliveryMessage
    ) {
        Order order = new Order();
        order.user = user;
        order.userCoupon = userCoupon;
        order.totalProductPrice = totalProductPrice;
        order.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        order.deliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        order.finalPrice = finalPrice;
        order.recipientName = recipientName;
        order.recipientPhone = recipientPhone;
        order.deliveryAddress = deliveryAddress;
        order.deliveryDetailAddress = deliveryDetailAddress;
        order.deliveryMessage = deliveryMessage;
        order.orderStatus = OrderStatus.PENDING;
        return order;
    }

    public void cancel(String cancelReason) {
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelReason = cancelReason;
    }

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

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}