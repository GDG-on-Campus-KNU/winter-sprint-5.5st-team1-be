package com.gdg.sprint.team1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gdg.sprint.team1.domain.cart.CartItem;
import com.gdg.sprint.team1.domain.cart.CartItemId;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

    List<CartItem> findAllByIdUserId(Integer userId);

    Optional<CartItem> findByIdUserIdAndIdProductId(Integer userId, Integer productId);

    void deleteByIdUserIdAndIdProductId(Integer userId, Integer productId);

    @Modifying
    @Query("""
        update CartItem c
        set c.quantity = c.quantity + :quantity
        where c.id.userId = :userId and c.id.productId = :productId
        """)
    int incrementQuantity(@Param("userId") Integer userId,
                          @Param("productId") Integer productId,
                          @Param("quantity") Integer quantity);

    @Modifying
    @Query("delete from CartItem c where c.id.userId = :userId and c.id.productId in :productIds")
    void deleteByUserIdAndProductIds(@Param("userId") Integer userId, @Param("productIds") List<Integer> productIds);

}
