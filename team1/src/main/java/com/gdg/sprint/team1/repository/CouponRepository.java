package com.gdg.sprint.team1.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdg.sprint.team1.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {
}
