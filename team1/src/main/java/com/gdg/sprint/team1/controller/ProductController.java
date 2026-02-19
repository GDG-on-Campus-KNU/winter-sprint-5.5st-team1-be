package com.gdg.sprint.team1.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.controller.api.ProductApi;
import com.gdg.sprint.team1.dto.product.ProductDetailDto;
import com.gdg.sprint.team1.dto.product.ProductListResponse;
import com.gdg.sprint.team1.service.ProductService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;

    @Override
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<ProductListResponse>> getProductList(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) BigDecimal min_price,
        @RequestParam(required = false) BigDecimal max_price,
        @RequestParam(required = false, name = "exclude_sold_out") Boolean excludeSoldOut,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order
    ) {
        ProductListResponse data = productService.getProductList(
            page, limit, status, search,
            min_price, max_price, excludeSoldOut, sort, order
        );
        String message = data.pagination().totalItems() == 0
            ? "검색 결과가 없습니다."
            : "상품 목록 조회 성공";
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    @Override
    @GetMapping("/products/{product_id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductDetail(
        @PathVariable("product_id") Long productId
    ) {
        ProductDetailDto data = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(data, "상품 상세 조회 성공"));
    }
}
