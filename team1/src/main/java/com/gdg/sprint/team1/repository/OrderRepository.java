package com.gdg.sprint.team1.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gdg.sprint.team1.entity.Order;
import com.gdg.sprint.team1.entity.Order.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @EntityGraph(attributePaths = {"orderItems"})
    Page<Order> findAllByUserId(Integer userId, Pageable pageable);

    @EntityGraph(attributePaths = {"orderItems"})
    Page<Order> findAllByUserIdAndOrderStatus(Integer userId, OrderStatus orderStatus, Pageable pageable);

    @EntityGraph(attributePaths = {
        "user",
        "userCoupon",
        "userCoupon.coupon",
        "orderItems",
        "orderItems.product"
    })
    Optional<Order> findWithDetailsById(Integer orderId);

    @EntityGraph(attributePaths = {"orderItems"})
    Page<Order> findAllByUserIdAndCreatedAtGreaterThanEqual(
            Integer userId,
            LocalDateTime fromDate,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"orderItems"})
    Page<Order> findAllByUserIdAndOrderStatusAndCreatedAtGreaterThanEqual(
            Integer userId,
            OrderStatus orderStatus,
            LocalDateTime fromDate,
            Pageable pageable
    );
}
