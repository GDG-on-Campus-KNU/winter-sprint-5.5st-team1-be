package com.gdg.sprint.team1.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.controller.api.OrderApi;
import com.gdg.sprint.team1.dto.order.CancelOrderRequest;
import com.gdg.sprint.team1.dto.order.CancelOrderResponse;
import com.gdg.sprint.team1.dto.order.CreateOrderFromCartRequest;
import com.gdg.sprint.team1.dto.order.CreateOrderRequest;
import com.gdg.sprint.team1.dto.order.CreateOrderResponse;
import com.gdg.sprint.team1.dto.order.OrderDetailResponse;
import com.gdg.sprint.team1.dto.order.OrderResponse;
import com.gdg.sprint.team1.security.CurrentUser;
import com.gdg.sprint.team1.security.UserContextHolder;
import com.gdg.sprint.team1.service.OrderService;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
        @CurrentUser UserContextHolder.UserContext user,
        @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderResponse response = orderService.createOrder(user.userId(), request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "주문이 생성되었습니다."));
    }

    @Override
    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrderFromCart(
        @CurrentUser UserContextHolder.UserContext user,
        @Valid @RequestBody CreateOrderFromCartRequest request
    ) {
        CreateOrderResponse response = orderService.createOrderFromCart(user.userId(), request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "주문이 생성되었습니다."));
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(
        @CurrentUser UserContextHolder.UserContext user,
        @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,
        @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) Integer limit,
        @RequestParam(required = false) String status
    ) {
        Page<OrderResponse> response = orderService.getOrders(user.userId(), page, limit, status);
        return ResponseEntity.ok(ApiResponse.success(response, "주문 목록 조회 성공"));
    }

    @Override
    @GetMapping("/{order_id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
        @CurrentUser UserContextHolder.UserContext user,
        @PathVariable("order_id") @Positive Integer orderId
    ) {
        OrderDetailResponse response = orderService.getOrderDetail(user.userId(), orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "주문 상세 조회 성공"));
    }

    @Override
    @PatchMapping("/{order_id}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
        @CurrentUser UserContextHolder.UserContext user,
        @PathVariable("order_id") @Positive Integer orderId,
        @Valid @RequestBody CancelOrderRequest request
    ) {
        CancelOrderResponse response = orderService.cancelOrder(user.userId(), orderId, request.cancelReason());
        return ResponseEntity.ok(ApiResponse.success(response, "주문이 취소되었습니다."));
    }
}
