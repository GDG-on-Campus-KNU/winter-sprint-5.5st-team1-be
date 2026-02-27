package com.gdg.sprint.team1.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;

import lombok.Getter;

import com.gdg.sprint.team1.exception.InsufficientStockException;

@Entity
@Table(name = "products")
public class Product {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(nullable = false, length = 255)
    private String name;

    @Getter
    @Column(columnDefinition = "TEXT")
    private String description;

    @Getter
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Getter
    @Column(nullable = false)
    private Integer stock = 0;

    @Getter
    @Column(name = "product_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus = ProductStatus.ACTIVE;

    @Getter
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Getter
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Getter
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Product create(
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }

        Product product = new Product();
        product.name = name;
        product.description = description;
        product.price = price;
        product.stock = stock;
        product.imageUrl = imageUrl;
        product.productStatus = (stock > 0) ? ProductStatus.ACTIVE : ProductStatus.SOLD_OUT;

        return product;
    }

    public void update(String name, String description, BigDecimal price, Integer stock) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null) {
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
            }
            this.price = price;
        }
        if (stock != null) {
            if (stock < 0) {
                throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
            }
            this.stock = stock;
            updateStatusByStock();
        }
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private void updateStatusByStock() {
        if (this.stock > 0 && this.productStatus == ProductStatus.SOLD_OUT) {
            this.productStatus = ProductStatus.ACTIVE;
        } else if (this.stock == 0 && this.productStatus == ProductStatus.ACTIVE) {
            this.productStatus = ProductStatus.SOLD_OUT;
        }
    }

    public void markAsInactive() {
        this.productStatus = ProductStatus.INACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void deductStock(int quantity) {
        int current = this.stock != null ? this.stock : 0;
        if (current < quantity) {
            throw new InsufficientStockException(getName(), quantity, current);
        }
        this.stock = current - quantity;
        updateStatusByStock();
    }

    public void restoreStock(int quantity) {
        int current = this.stock != null ? this.stock : 0;
        this.stock = current + quantity;
        updateStatusByStock();
    }
    public enum ProductStatus {
        ACTIVE,
        SOLD_OUT,
        INACTIVE
    }
}
