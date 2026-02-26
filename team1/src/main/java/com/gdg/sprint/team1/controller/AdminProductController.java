package com.gdg.sprint.team1.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.product.ProductListResponse;
import com.gdg.sprint.team1.service.ProductService;

@RestController
@RequestMapping("/api/v1/admin/products")
@Validated
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProductListResponse>> getAdminProducts(
            @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        ProductListResponse data = productService.getAdminProductList(page, limit, status, search);

        String message = data.pagination().totalItems() == 0
                ? "검색 결과가 없습니다."
                : "관리자 상품 목록 조회 성공";

        return ResponseEntity.ok(ApiResponse.success(data, message));
    }
}
