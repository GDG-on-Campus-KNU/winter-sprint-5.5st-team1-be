package com.gdg.sprint.team1.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdg.sprint.team1.cart.domain.CartItem;
import com.gdg.sprint.team1.cart.domain.CartItemId;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

    List<CartItem> findAllByIdUserId(Integer userId);

    Optional<CartItem> findByIdUserIdAndIdProductId(Integer userId, Integer productId);

    void deleteByIdUserIdAndIdProductId(Integer userId, Integer productId);

}
