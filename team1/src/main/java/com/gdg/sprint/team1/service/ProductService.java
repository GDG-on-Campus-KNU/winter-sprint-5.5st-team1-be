package com.gdg.sprint.team1.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.dto.product.*;
import com.gdg.sprint.team1.entity.Product;
import com.gdg.sprint.team1.exception.ProductNotFoundException;
import com.gdg.sprint.team1.repository.ProductRepository;
import com.gdg.sprint.team1.repository.ProductSpecs;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final String DEFAULT_SORT = "created_at";
    private static final String DEFAULT_ORDER = "desc";

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductListResponse getProductList(
        Integer page,
        Integer limit,
        String status,
        String search,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean excludeSoldOut,
        String sort,
        String order
    ) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        String safeSort = sort == null || sort.isBlank() ? DEFAULT_SORT : sort;
        String safeOrder = order == null || order.isBlank() ? DEFAULT_ORDER : order.toLowerCase();
        boolean exclude = Boolean.TRUE.equals(excludeSoldOut);

        Specification<Product> spec = Specification
            .where(ProductSpecs.status(status))
            .and(ProductSpecs.minPrice(minPrice))
            .and(ProductSpecs.maxPrice(maxPrice))
            .and(ProductSpecs.excludeSoldOut(exclude))
            .and(ProductSpecs.search(search));

        Sort.Direction direction = "asc".equals(safeOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = mapSortProperty(safeSort);
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(direction, sortProperty));

        var productPage = productRepository.findAll(spec, pageable);
        List<ProductListDto> products = productPage.getContent().stream()
            .map(this::toListDto)
            .collect(Collectors.toList());

        Map<String, Object> filtersApplied = new HashMap<>();
        if (minPrice != null) filtersApplied.put("min_price", minPrice);
        if (maxPrice != null) filtersApplied.put("max_price", maxPrice);
        if (status != null && !status.isBlank()) filtersApplied.put("status", status);
        if (exclude) filtersApplied.put("exclude_sold_out", true);

        SearchInfo searchInfo = new SearchInfo(
            search != null ? search : null,
            productPage.getTotalElements(),
            filtersApplied.isEmpty() ? null : filtersApplied
        );

        PaginationInfo pagination = new PaginationInfo(
            productPage.getNumber() + 1,
            productPage.getTotalPages(),
            productPage.getTotalElements(),
            productPage.getSize(),
            productPage.hasNext(),
            productPage.hasPrevious()
        );

        return new ProductListResponse(products, searchInfo, pagination);
    }

    @Transactional(readOnly = true)
    public ProductDetailDto getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
        return toDetailDto(product);
    }

    private ProductListDto toListDto(Product p) {
        return new ProductListDto(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getPrice(),
            p.getStock(),
            p.getProductStatus().name(),
            p.getImageUrl(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }

    private ProductDetailDto toDetailDto(Product p) {
        return new ProductDetailDto(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getPrice(),
            p.getStock(),
            p.getProductStatus().name(),
            p.getImageUrl(),
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }

    private String mapSortProperty(String sort) {
        return switch (sort.toLowerCase()) {
            case "price" -> "price";
            case "name" -> "name";
            default -> "createdAt";
        };
    }
    @Transactional(readOnly = true)
    public ProductListResponse getAdminProductList(
        Integer page,
        Integer limit,
        String status,
        String search
    ) {
        return getProductList(page, limit, status, search, null, null, null, null, null);
    }
}
