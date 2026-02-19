package com.gdg.sprint.team1.controller.api;

import org.springframework.http.ResponseEntity;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.cart.AddCartItemRequest;
import com.gdg.sprint.team1.dto.cart.CartResponse;
import com.gdg.sprint.team1.dto.cart.DeleteCartItemsRequest;
import com.gdg.sprint.team1.dto.cart.UpdateCartItemRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@Tag(name = "장바구니 API", description = "장바구니 조회/추가/수정/삭제 (JWT 인증 필요)")
@SecurityRequirement(name = "bearerAuth")
public interface CartApi {

    @Operation(summary = "장바구니 조회", description = "JWT 인증 사용자 기준 장바구니 목록과 요약 정보 조회")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<CartResponse>> getCart();

    @Operation(summary = "장바구니 담기", description = "상품을 장바구니에 추가")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "담기 성공")
    })
    ResponseEntity<ApiResponse<Void>> addItem(@Valid AddCartItemRequest request);

    @Operation(summary = "장바구니 수량 변경", description = "상품 수량 변경. 0 이하일 경우 삭제")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공")
    })
    ResponseEntity<ApiResponse<Void>> updateQuantity(@Positive Integer productId, @Valid UpdateCartItemRequest request);

    @Operation(summary = "장바구니 선택 삭제", description = "상품 ID 목록으로 선택 삭제")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공")
    })
    ResponseEntity<ApiResponse<Void>> deleteSelected(@Valid DeleteCartItemsRequest request);

    @Operation(summary = "장바구니 단일 삭제", description = "상품 ID로 장바구니 항목 삭제")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공")
    })
    ResponseEntity<ApiResponse<Void>> deleteItem(@Positive Integer productId);

    @Operation(summary = "장바구니 전체 삭제", description = "현재 사용자 장바구니 전체 비우기")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공")
    })
    ResponseEntity<ApiResponse<Void>> deleteAll();
}
