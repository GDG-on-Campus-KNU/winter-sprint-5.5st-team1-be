package com.gdg.sprint.team1.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.controller.api.MyPageApi;
import com.gdg.sprint.team1.dto.auth.UserMeResponse;
import com.gdg.sprint.team1.dto.my.MyCouponResponse;
import com.gdg.sprint.team1.exception.AuthRequiredException;
import com.gdg.sprint.team1.security.UserContextHolder;
import com.gdg.sprint.team1.service.UserCouponService;
import com.gdg.sprint.team1.service.UserService;

@RestController
@RequestMapping("/api/v1/my")
@RequiredArgsConstructor
public class MyPageController implements MyPageApi {

    private final UserService userService;
    private final UserCouponService userCouponService;

    private Integer currentUserId() {
        Integer userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new AuthRequiredException();
        }
        return userId;
    }

    @Override
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserMeResponse>> getMyInfo() {
        Integer userId = currentUserId();
        UserMeResponse data = UserMeResponse.from(userService.findById(userId));
        return ResponseEntity.ok(ApiResponse.success(data, "조회 성공"));
    }

    @Override
    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<List<MyCouponResponse>>> getMyCoupons(@RequestParam(required = false) String status) {
        Integer userId = currentUserId();
        List<MyCouponResponse> data = userCouponService.findCouponsByUserId(userId, status)
            .stream()
            .map(MyCouponResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(data, "쿠폰 목록 조회 성공"));
    }
}
