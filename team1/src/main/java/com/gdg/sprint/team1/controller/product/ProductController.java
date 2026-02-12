package com.gdg.sprint.team1.controller.product;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.product.*;
import com.gdg.sprint.team1.service.product.ProductService;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<ProductListResponse>> getProductList(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Long store_id,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) BigDecimal min_price,
        @RequestParam(required = false) BigDecimal max_price,
        @RequestParam(required = false, name = "exclude_sold_out") Boolean excludeSoldOut,
        @RequestParam(required = false) String sort,
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

    @GetMapping("/products/{product_id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductDetail(@PathVariable("product_id") Long productId) {
        ProductDetailDto data = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(data, "상품 상세 조회 성공"));
    }

    @GetMapping("/stores/{store_id}/products")
    public ResponseEntity<ApiResponse<StoreProductListResponse>> getStoreProducts(
        @PathVariable("store_id") Long storeId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String status
    ) {
        StoreProductListResponse data = productService.getProductsByStoreId(storeId, page, limit, status);
        return ResponseEntity.ok(ApiResponse.success(data, "상점 상품 목록 조회 성공"));
    }
}
