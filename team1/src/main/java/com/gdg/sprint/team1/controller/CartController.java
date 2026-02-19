package com.gdg.sprint.team1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.controller.api.CartApi;
import com.gdg.sprint.team1.dto.cart.AddCartItemRequest;
import com.gdg.sprint.team1.dto.cart.CartResponse;
import com.gdg.sprint.team1.dto.cart.DeleteCartItemsRequest;
import com.gdg.sprint.team1.dto.cart.UpdateCartItemRequest;
import com.gdg.sprint.team1.service.CartService;

@RestController
@RequestMapping("/api/v1/cart")
@Validated
@RequiredArgsConstructor
public class CartController implements CartApi {

    private final CartService cartService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(), "장바구니 조회 성공"));
    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addItem(@Valid @RequestBody AddCartItemRequest request) {
        cartService.addItem(request.productId(), request.quantity());
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 담기 성공"));
    }

    @Override
    @PatchMapping("/items/{product_id}")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(
        @PathVariable("product_id") @Positive Long productId,
        @Valid @RequestBody UpdateCartItemRequest request
    ) {
        cartService.updateQuantity(productId, request.quantity());
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 수량 변경 성공"));
    }

    @Override
    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<Void>> deleteSelected(@Valid @RequestBody DeleteCartItemsRequest request) {
        cartService.deleteSelected(request.itemIds());
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 선택 삭제 성공"));
    }

    @Override
    @DeleteMapping("/items/{product_id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable("product_id") @Positive Long productId) {
        cartService.deleteItem(productId);
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 상품 삭제 성공"));
    }

    @Override
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAll() {
        cartService.deleteAll();
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 전체 삭제 성공"));
    }
}
