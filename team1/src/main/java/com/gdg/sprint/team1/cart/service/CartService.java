package com.gdg.sprint.team1.cart.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gdg.sprint.team1.cart.domain.CartItem;
import com.gdg.sprint.team1.cart.repository.CartItemRepository;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;

    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItem> getCartItems(Integer userId) {
        return cartItemRepository.findAllByIdUserId(userId);
    }

    public int calculateDeliveryFee(int totalProductPrice) {
        if (totalProductPrice == 0) return 0;
        return totalProductPrice >= 30000 ? 0 : 3000;
    }

}
