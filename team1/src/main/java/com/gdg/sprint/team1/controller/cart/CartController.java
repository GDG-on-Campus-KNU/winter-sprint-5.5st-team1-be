package com.gdg.sprint.team1.controller.cart;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.cart.AddCartItemRequest;
import com.gdg.sprint.team1.dto.cart.CartResponse;
import com.gdg.sprint.team1.dto.cart.DeleteCartItemsRequest;
import com.gdg.sprint.team1.dto.cart.UpdateCartItemRequest;
import com.gdg.sprint.team1.service.cart.CartService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader("X-USER-ID") Integer userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(userId), "장바구니 조회 성공"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addItem(
            @RequestHeader("X-USER-ID") Integer userId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        cartService.addItem(userId, request.productId(), request.quantity());
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 담기 성공"));
    }

    @PatchMapping("/items/{product_id}")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(
            @RequestHeader("X-USER-ID") Integer userId,
            @PathVariable("product_id") Integer productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        cartService.updateQuantity(userId, productId, request.quantity());
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 수량 변경 성공"));
    }

    // 선택 삭제
    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<Void>> deleteSelected(
            @RequestHeader("X-USER-ID") Integer userId,
            @Valid @RequestBody DeleteCartItemsRequest request
    ) {
        cartService.deleteSelected(userId, request.itemIds());
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 선택 삭제 성공"));
    }

    // 전체 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAll(
            @RequestHeader("X-USER-ID") Integer userId
    ) {
        cartService.deleteAll(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "장바구니 전체 삭제 성공"));
    }

}
