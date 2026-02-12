package com.gdg.sprint.team1.cart.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gdg.sprint.team1.cart.domain.CartItem;
import com.gdg.sprint.team1.cart.service.CartService;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartItem> getCart(@RequestHeader("X-USER-ID") Integer userId) {
        return cartService.getCartItems(userId);
    }

}