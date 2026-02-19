package com.gdg.sprint.team1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gdg.sprint.team1.entity.UserCoupon;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Integer> {

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon WHERE uc.user.id = :userId AND uc.usedAt IS NULL AND uc.expiredAt > CURRENT_TIMESTAMP")
    List<UserCoupon> findAvailableCouponsByUserId(@Param("userId") Integer userId);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon WHERE uc.user.id = :userId ORDER BY uc.issuedAt DESC")
    List<UserCoupon> findAllByUserId(@Param("userId") Integer userId);
}
