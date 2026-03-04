package com.gdg.sprint.team1.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.controller.api.AdminProductApi;
import com.gdg.sprint.team1.dto.admin.CreateProductRequest;
import com.gdg.sprint.team1.dto.product.ProductDetailDto;
import com.gdg.sprint.team1.dto.product.ProductListResponse;
import com.gdg.sprint.team1.dto.admin.UpdateProductRequest;
import com.gdg.sprint.team1.service.AdminProductService;
import com.gdg.sprint.team1.entity.Product;

@RestController
@RequestMapping("/api/v1/admin/products")
@Validated
@RequiredArgsConstructor
public class AdminProductController implements AdminProductApi {

    private final AdminProductService adminProductService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<ProductListResponse>> getAdminProducts(
        @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,
        @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer limit,
        @RequestParam(required = false) Product.ProductStatus status,
        @RequestParam(required = false) String search) {

        ProductListResponse data = adminProductService.getAdminProductList(page, limit, status, search);

        String message = data.pagination().totalItems() == 0
            ? "검색 결과가 없습니다."
            : "관리자 상품 목록 조회 성공";

        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    @Override
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getAdminProductDetail(
        @PathVariable Long productId) {

        ProductDetailDto data = adminProductService.getAdminProductDetail(productId);
        return ResponseEntity.ok(ApiResponse.success(data, "상품 상세 조회 성공"));
    }

    @Override
    @PostMapping(
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<ProductDetailDto>> createProduct(
        @RequestPart("request") @Valid CreateProductRequest request,
        @RequestParam(value = "image", required = false) MultipartFile image) {

        ProductDetailDto response = adminProductService.createProduct(request, image);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "상품이 등록되었습니다."));
    }

    @Override
    @PatchMapping(
        value = "/{productId}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<ProductDetailDto>> updateProduct(
        @PathVariable Long productId,
        @RequestPart("request") @Valid UpdateProductRequest request,
        @RequestParam(value = "image", required = false) MultipartFile image) {

        ProductDetailDto response = adminProductService.updateProduct(productId, request, image);
        return ResponseEntity.ok(ApiResponse.success(response, "상품이 수정되었습니다."));
    }

    @Override
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        adminProductService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(null, "상품이 삭제되었습니다."));
    }
}
