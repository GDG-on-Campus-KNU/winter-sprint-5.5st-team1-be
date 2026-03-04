package com.gdg.sprint.team1.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.admin.CreateProductRequest;
import com.gdg.sprint.team1.dto.product.ProductDetailDto;
import com.gdg.sprint.team1.dto.product.ProductListResponse;
import com.gdg.sprint.team1.dto.admin.UpdateProductRequest;
import com.gdg.sprint.team1.entity.Product;

import com.gdg.sprint.team1.config.SwaggerBody;

@Tag(name = "관리자 상품 API", description = "관리자 전용 상품 CRUD")
@SecurityRequirement(name = "bearerAuth")
public interface AdminProductApi {

    @Operation(summary = "관리자 상품 목록 조회")
    ResponseEntity<ApiResponse<ProductListResponse>> getAdminProducts(
        @Parameter(description = "페이지 번호", example = "1")
        @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,

        @Parameter(description = "페이지당 항목 수", example = "20")
        @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer limit,

        @Parameter(
            description = "상품 상태 (전체 조회는 선택 안 함)",
            schema = @Schema(implementation = Product.ProductStatus.class)
        )
        @RequestParam(required = false) Product.ProductStatus status,

        @Parameter(description = "검색어", example = "노트북")
        @RequestParam(required = false) String search
    );

    @Operation(summary = "관리자 상품 상세 조회")
    ResponseEntity<ApiResponse<ProductDetailDto>> getAdminProductDetail(
        @Parameter(description = "상품 ID", example = "1")
        @PathVariable Long productId
    );

    @Operation(summary = "상품 등록", description = "이미지 포함 상품 등록")
    @SwaggerBody(content = @Content(
        encoding = {
            @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE),
            @Encoding(name = "image", contentType = "image/png, image/jpeg, image/gif")
        }
    ))
    ResponseEntity<ApiResponse<ProductDetailDto>> createProduct(
        @RequestPart("request") @Valid CreateProductRequest request,

        @Parameter(
            description = "상품 이미지 (선택)",
            required = false,
            schema = @Schema(
                type = "string",
                format = "binary",
                nullable = true
            )
        )
        @RequestParam(value = "image", required = false) MultipartFile image
    );

    @Operation(summary = "상품 수정")
    @SwaggerBody(content = @Content(
        encoding = {
            @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE),
            @Encoding(name = "image", contentType = "image/png, image/jpeg, image/gif")
        }
    ))
    ResponseEntity<ApiResponse<ProductDetailDto>> updateProduct(
        @Parameter(description = "상품 ID", example = "1")
        @PathVariable Long productId,

        @RequestPart("request") @Valid UpdateProductRequest request,

        @Parameter(
            description = "새 상품 이미지 (선택)",
            required = false,
            schema = @Schema(
                type = "string",
                format = "binary",
                nullable = true
            )
        )
        @RequestParam(value = "image", required = false) MultipartFile image
    );

    @Operation(summary = "상품 삭제")
    ResponseEntity<ApiResponse<Void>> deleteProduct(
        @Parameter(description = "상품 ID", example = "1")
        @PathVariable Long productId
    );
}