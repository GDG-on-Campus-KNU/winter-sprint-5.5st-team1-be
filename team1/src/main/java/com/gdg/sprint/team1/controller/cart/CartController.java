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

import com.gdg.sprint.team1.dto.cart.AddCartItemRequest;
import com.gdg.sprint.team1.dto.cart.CartResponse;
import com.gdg.sprint.team1.dto.cart.CartSummary;
import com.gdg.sprint.team1.dto.cart.DeleteCartItemsRequest;
import com.gdg.sprint.team1.dto.cart.UpdateCartItemRequest;
import com.gdg.sprint.team1.service.cart.CartService;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader("X-USER-ID") Integer userId
    ) {
        // TODO: cartService.getCart(userId)
        return ResponseEntity.ok(
                new CartResponse(userId, java.util.List.of(),
                        new CartSummary(0, 0, 0, 0, 0))
        );
    }

    @PostMapping
    public ResponseEntity<Void> addItem(
            @RequestHeader("X-USER-ID") Integer userId,
            @RequestBody AddCartItemRequest request
    ) {
        // TODO: cartService.addItem(userId, request.productId(), request.quantity())
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Void> updateQuantity(
            @RequestHeader("X-USER-ID") Integer userId,
            @PathVariable Integer itemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        // TODO: cartService.updateQuantity(userId, itemId, request.quantity())
        // if quantity <= 0 => delete
        return ResponseEntity.ok().build();
    }

    // 선택 삭제
    @DeleteMapping("/items")
    public ResponseEntity<Void> deleteSelected(
            @RequestHeader("X-USER-ID") Integer userId,
            @RequestBody DeleteCartItemsRequest request
    ) {
        // TODO: cartService.deleteSelected(userId, request.itemIds())
        return ResponseEntity.ok().build();
    }

    // 전체 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteAll(
            @RequestHeader("X-USER-ID") Integer userId
    ) {
        // TODO: cartService.deleteAll(userId)
        return ResponseEntity.ok().build();
    }

}