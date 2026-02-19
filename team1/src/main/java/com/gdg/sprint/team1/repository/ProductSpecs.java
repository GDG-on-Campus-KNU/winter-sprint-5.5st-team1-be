package com.gdg.sprint.team1.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.gdg.sprint.team1.entity.Product;

public final class ProductSpecs {

    private ProductSpecs() {}

    public static Specification<Product> status(String status) {
        if (status == null || status.isBlank()) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("productStatus"), status);
    }

    public static Specification<Product> minPrice(BigDecimal minPrice) {
        if (minPrice == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> maxPrice(BigDecimal maxPrice) {
        if (maxPrice == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> excludeSoldOut(boolean exclude) {
        if (!exclude) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.greaterThan(root.get("stock"), 0);
    }

    public static Specification<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return (root, query, cb) -> cb.conjunction();
        String escaped = keyword.trim()
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
        String pattern = "%" + escaped + "%";
        return (root, query, cb) -> cb.or(
            cb.like(root.get("name"), pattern, '\\'),
            cb.like(root.get("description"), pattern, '\\')
        );
    }
}
