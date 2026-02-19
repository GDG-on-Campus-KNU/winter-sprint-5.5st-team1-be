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

@Entity
@Table(name = "orders")
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
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_detail_address")
    private String deliveryDetailAddress;

    @Column(name = "delivery_message", length = 500)
    private String deliveryMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public UserCoupon getUserCoupon() { return userCoupon; }
    public void setUserCoupon(UserCoupon userCoupon) { this.userCoupon = userCoupon; }
    public BigDecimal getTotalProductPrice() { return totalProductPrice; }
    public void setTotalProductPrice(BigDecimal totalProductPrice) { this.totalProductPrice = totalProductPrice; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getDeliveryDetailAddress() { return deliveryDetailAddress; }
    public void setDeliveryDetailAddress(String deliveryDetailAddress) { this.deliveryDetailAddress = deliveryDetailAddress; }
    public String getDeliveryMessage() { return deliveryMessage; }
    public void setDeliveryMessage(String deliveryMessage) { this.deliveryMessage = deliveryMessage; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}