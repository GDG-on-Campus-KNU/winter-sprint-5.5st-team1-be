package com.gdg.sprint.team1.domain.cart;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CartItemId implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "product_id")
    private Integer productId;

    protected CartItemId() {}

    public CartItemId(Integer userId, Integer productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public Integer getUserId() { return userId; }
    public Integer getProductId() { return productId; }

    protected void setUserId(Integer userId) { this.userId = userId; }
    protected void setProductId(Integer productId) { this.productId = productId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItemId)) return false;
        CartItemId that = (CartItemId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, productId);
    }
}
