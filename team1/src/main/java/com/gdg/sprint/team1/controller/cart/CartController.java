package com.gdg.sprint.team1.controller.cart;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.cart.AddCartItemRequest;
import com.gdg.sprint.team1.dto.cart.CartResponse;
import com.gdg.sprint.team1.dto.cart.DeleteCartItemsRequest;
import com.gdg.sprint.team1.dto.cart.UpdateCartItemRequest;
import com.gdg.sprint.team1.service.cart.CartService;

@Tag(name = "장바구니 API", description = "장바구니 조회/추가/수정/삭제 API (JWT 인증 필요)")
@RestController
@RequestMapping("/api/v1/cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "장바구니 조회", description = "JWT 인증 사용자 기준 장바구니 목록과 요약 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(), "장바구니 조회 성공"));
    }

    @PostMapping
    @Operation(summary = "장바구니 담기", description = "상품을 장바구니에 추가합니다.")
    public ResponseEntity<ApiResponse<Void>> addItem(@Valid @RequestBody AddCartItemRequest request) {
        cartService.addItem(request.productId(), request.quantity());
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 담기 성공"));
    }

    @PatchMapping("/items/{product_id}")
    @Operation(summary = "장바구니 수량 변경", description = "상품 수량을 변경합니다. 0 이하일 경우 삭제됩니다.")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(
            @PathVariable("product_id") Integer productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        cartService.updateQuantity(productId, request.quantity());
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 수량 변경 성공"));
    }

    @DeleteMapping("/items")
    @Operation(summary = "장바구니 선택 삭제", description = "상품 ID 목록을 받아 선택 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteSelected(
            @Valid @RequestBody DeleteCartItemsRequest request
    ) {
        cartService.deleteSelected(request.itemIds());
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 선택 삭제 성공"));
    }

    @DeleteMapping("/items/{product_id}")
    @Operation(summary = "장바구니 단일 삭제", description = "상품 ID로 장바구니 항목을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable("product_id") Integer productId) {
        cartService.deleteItem(productId);
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 상품 삭제 성공"));
    }

    @DeleteMapping
    @Operation(summary = "장바구니 전체 삭제", description = "현재 사용자의 장바구니를 전체 비웁니다.")
    public ResponseEntity<ApiResponse<Void>> deleteAll() {
        cartService.deleteAll();
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 전체 삭제 성공"));
    }

}
