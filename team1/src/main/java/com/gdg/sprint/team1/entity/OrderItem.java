package com.gdg.sprint.team1.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;

import com.gdg.sprint.team1.domain.order.OrderItemId;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Getter
    @EmbeddedId
    private OrderItemId id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Getter
    @Column(nullable = false)
    private Integer quantity;

    @Getter
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Getter
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected OrderItem() {}

    public OrderItem(Order order, Product product, Integer quantity, BigDecimal unitPrice) {
        this.id = new OrderItemId(order.getId(), product.getId());
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void setOrder(Order order) { 
        this.order = order;
        if (order != null && this.product != null) {
            this.id = new OrderItemId(order.getId(), this.product.getId());
        }
    }
    public void setProduct(Product product) { 
        this.product = product;
        if (this.order != null && product != null) {
            this.id = new OrderItemId(this.order.getId(), product.getId());
        }
    }
}
