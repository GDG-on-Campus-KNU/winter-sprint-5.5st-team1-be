package com.gdg.sprint.team1.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gdg.sprint.team1.entity.UserCoupon;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Integer> {

    @EntityGraph(attributePaths = "coupon")
    List<UserCoupon> findByUser_IdAndUsedAtIsNullAndExpiredAtAfter(Integer userId, LocalDateTime now);

    @EntityGraph(attributePaths = "coupon")
    List<UserCoupon> findByUser_IdOrderByIssuedAtDesc(Integer userId);
}
