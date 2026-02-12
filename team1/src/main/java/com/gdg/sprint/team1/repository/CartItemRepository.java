package com.gdg.sprint.team1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdg.sprint.team1.domain.cart.CartItem;
import com.gdg.sprint.team1.domain.cart.CartItemId;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

    List<CartItem> findAllByIdUserId(Integer userId);

    Optional<CartItem> findByIdUserIdAndIdProductId(Integer userId, Integer productId);

    void deleteByIdUserIdAndIdProductId(Integer userId, Integer productId);

}
