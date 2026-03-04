package com.gdg.sprint.team1.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.controller.api.MyPageApi;
import com.gdg.sprint.team1.dto.auth.UserMeResponse;
import com.gdg.sprint.team1.dto.my.MyCouponResponse;
import com.gdg.sprint.team1.dto.my.UpdateMyInfoRequest;
import com.gdg.sprint.team1.dto.order.OrderDetailResponse;
import com.gdg.sprint.team1.dto.order.OrderResponse;
import com.gdg.sprint.team1.security.CurrentUser;
import com.gdg.sprint.team1.security.UserContextHolder;
import com.gdg.sprint.team1.service.OrderService;
import com.gdg.sprint.team1.service.UserCouponService;
import com.gdg.sprint.team1.service.UserService;
import com.gdg.sprint.team1.entity.User;

@RestController
@RequestMapping("/api/v1/my")
@Validated
@RequiredArgsConstructor
public class MyPageController implements MyPageApi {

    private final UserService userService;
    private final UserCouponService userCouponService;
    private final OrderService orderService;

    @Override
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserMeResponse>> getMyInfo(
        @CurrentUser UserContextHolder.UserContext user
    ) {
        UserMeResponse data = UserMeResponse.from(userService.findById(user.userId()));
        return ResponseEntity.ok(ApiResponse.success(data, "조회 성공"));
    }

    @Override
    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<List<MyCouponResponse>>> getMyCoupons(
        @CurrentUser UserContextHolder.UserContext user,
        @RequestParam(required = false) String status
    ) {
        List<MyCouponResponse> data = userCouponService.findCouponsByUserId(user.userId(), status)
            .stream()
            .map(MyCouponResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(data, "쿠폰 목록 조회 성공"));
    }

    @Override
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @CurrentUser UserContextHolder.UserContext user,
            @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer months
    ) {
        Page<OrderResponse> data = orderService.getMyOrders(user.userId(), page, limit, status, months);
        return ResponseEntity.ok(ApiResponse.success(data, "주문 목록 조회 성공"));
    }

    @Override
    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getMyOrderDetail(
            @CurrentUser UserContextHolder.UserContext user,
            @PathVariable("id") @Positive Integer orderId
    ) {
        OrderDetailResponse data = orderService.getOrderDetail(user.userId(), orderId);
        return ResponseEntity.ok(ApiResponse.success(data, "주문 상세 조회 성공"));
    }

    @Override
    @PatchMapping("/info")
    public ResponseEntity<ApiResponse<UserMeResponse>> updateMyInfo(
            @CurrentUser UserContextHolder.UserContext user,
            @Valid @RequestBody UpdateMyInfoRequest request
    ) {
        User updated = userService.updateMyInfo(user.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(UserMeResponse.from(updated), "내 정보 수정 성공"));
    }
}
