package com.gdg.sprint.team1.controller.api;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.product.ProductDetailDto;
import com.gdg.sprint.team1.dto.product.ProductListResponse;

@Tag(name = "메뉴(상품) API", description = "상품 목록·상세 조회")
public interface ProductApi {

    @Operation(summary = "상품 목록 조회", description = "페이지네이션, 상태/가격 필터, 검색어, 정렬(최신순/가격순/이름순) 지원")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<ProductListResponse>> getProductList(
        @Parameter(description = "페이지 번호 (기본: 1)", example = "1") Integer page,
        @Parameter(description = "페이지당 항목 수 (기본: 20, 최대: 100)", example = "20") Integer limit,
        @Parameter(description = "상품 상태 (ACTIVE, INACTIVE)", example = "ACTIVE") String status,
        @Parameter(description = "검색 키워드 (상품명/설명)", example = "노트") String search,
        @Parameter(description = "최소 가격", example = "5000") BigDecimal min_price,
        @Parameter(description = "최대 가격", example = "15000") BigDecimal max_price,
        @Parameter(description = "품절 제외 여부", example = "true") Boolean excludeSoldOut,
        @Parameter(description = "정렬 기준 (created_at, price, name)", example = "price") String sort,
        @Parameter(description = "정렬 순서 (asc, desc)", example = "asc") String order
    );

    @Operation(summary = "상품 상세 조회", description = "상품 상세 정보(재고, 상태, 설명 등)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 없음")
    })
    ResponseEntity<ApiResponse<ProductDetailDto>> getProductDetail(
        @Parameter(description = "상품 ID", example = "1") Long productId
    );
}
