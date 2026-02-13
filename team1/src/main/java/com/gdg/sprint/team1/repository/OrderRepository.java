package com.gdg.sprint.team1.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gdg.sprint.team1.entity.Order;
import com.gdg.sprint.team1.entity.Order.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findAllByUserId(Integer userId, Pageable pageable);

    Page<Order> findAllByUserIdAndOrderStatus(Integer userId, OrderStatus orderStatus, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems LEFT JOIN FETCH o.userCoupon uc LEFT JOIN FETCH uc.coupon WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Integer orderId);
}
