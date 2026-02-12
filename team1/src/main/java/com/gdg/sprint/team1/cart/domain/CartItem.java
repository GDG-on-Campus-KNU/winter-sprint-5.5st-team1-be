package com.gdg.sprint.team1.cart.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "CartItems")
public class CartItem {

    @EmbeddedId
    private CartItemId id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected CartItem() {}

    public CartItem(Integer userId, Integer productId, Integer quantity) {
        this.id = new CartItemId(userId, productId);
        this.quantity = quantity;
    }
}
