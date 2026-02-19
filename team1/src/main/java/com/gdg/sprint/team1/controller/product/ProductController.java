package com.gdg.sprint.team1.controller.product;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.product.*;
import com.gdg.sprint.team1.service.product.ProductService;

@Tag(name = "메뉴(상품) API", description = "메뉴(상품) 목록·상세·상점별 조회 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(
        summary = "상품 목록 조회",
        description = "페이지네이션, 상점/상태/가격 필터, 검색어, 정렬(최신순/가격순/이름순)을 지원합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    })
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<ProductListResponse>> getProductList(
        @Parameter(description = "페이지 번호 (기본값: 1)", example = "1")
        @RequestParam(required = false) Integer page,
        @Parameter(description = "페이지당 항목 수 (기본값: 20, 최대: 100)", example = "20")
        @RequestParam(required = false) Integer limit,
        @Parameter(description = "상점 ID(특정 상점 상품만 조회)", example = "1")
        @RequestParam(required = false) Long store_id,
        @Parameter(description = "상품 상태 (ACTIVE, INACTIVE)", example = "ACTIVE")
        @RequestParam(required = false) String status,
        @Parameter(description = "검색 키워드 (상품명/설명 검색)", example = "김치")
        @RequestParam(required = false) String search,
        @Parameter(description = "최소 가격", example = "5000")
        @RequestParam(required = false) BigDecimal min_price,
        @Parameter(description = "최대 가격", example = "15000")
        @RequestParam(required = false) BigDecimal max_price,
        @Parameter(description = "품절 상품 제외 여부", example = "true")
        @RequestParam(required = false, name = "exclude_sold_out") Boolean excludeSoldOut,
        @Parameter(description = "정렬 기준 (created_at, price, name)", example = "price")
        @RequestParam(required = false) String sort,
        @Parameter(description = "정렬 순서 (asc, desc)", example = "asc")
        @RequestParam(required = false) String order
    ) {
        ProductListResponse data = productService.getProductList(
            page, limit, store_id, status, search,
            min_price, max_price, excludeSoldOut, sort, order
        );
        String message = data.pagination().totalItems() == 0
            ? "검색 결과가 없습니다."
            : "상품 목록 조회 성공";
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    @Operation(
        summary = "상품 상세 조회",
        description = "하나의 상품에 대한 상세 정보(재고, 상태, 설명, 상점 정보 등)를 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 상세 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @GetMapping("/products/{product_id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductDetail(
        @Parameter(description = "상품 ID", example = "1")
        @PathVariable("product_id") Long productId
    ) {
        ProductDetailDto data = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(data, "상품 상세 조회 성공"));
    }

    @Operation(
        summary = "상점별 상품 목록 조회",
        description = "특정 상점의 상품 목록을 페이지네이션하여 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상점 상품 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상점을 찾을 수 없음")
    })
    @GetMapping("/stores/{store_id}/products")
    public ResponseEntity<ApiResponse<StoreProductListResponse>> getStoreProducts(
        @Parameter(description = "상점 ID", example = "1")
        @PathVariable("store_id") Long storeId,
        @Parameter(description = "페이지 번호 (기본값: 1)", example = "1")
        @RequestParam(required = false) Integer page,
        @Parameter(description = "페이지당 항목 수 (기본값: 20)", example = "20")
        @RequestParam(required = false) Integer limit,
        @Parameter(description = "상품 상태 (ACTIVE, INACTIVE)", example = "ACTIVE")
        @RequestParam(required = false) String status
    ) {
        StoreProductListResponse data = productService.getProductsByStoreId(storeId, page, limit, status);
        return ResponseEntity.ok(ApiResponse.success(data, "상점 상품 목록 조회 성공"));
    }
}
