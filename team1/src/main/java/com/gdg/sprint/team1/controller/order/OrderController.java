package com.gdg.sprint.team1.controller.order;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.order.*;
import com.gdg.sprint.team1.service.order.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 주문 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDocs {

    private final OrderService orderService;

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
        @RequestHeader(value = "X-USER-ID") Integer userId,
        @RequestBody @Valid CreateOrderRequest request
    ) {
        CreateOrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "주문이 생성되었습니다."));
    }

    @PostMapping("/from-cart")
    @Override
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrderFromCart(
        @RequestHeader(value = "X-USER-ID") Integer userId,
        @RequestBody @Valid CreateOrderFromCartRequest request
    ) {
        CreateOrderResponse response = orderService.createOrderFromCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "주문이 생성되었습니다."));
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(
        @RequestHeader(value = "X-USER-ID") Integer userId,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer limit,
        @RequestParam(required = false) String status
    ) {
        Page<OrderResponse> response = orderService.getOrders(userId, page, limit, status);
        return ResponseEntity.ok(ApiResponse.success(response, "주문 목록 조회 성공"));
    }

    @GetMapping("/{order_id}")
    @Override
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
        @RequestHeader(value = "X-USER-ID") Integer userId,
        @PathVariable("order_id") Integer orderId
    ) {
        OrderDetailResponse response = orderService.getOrderDetail(userId, orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "주문 상세 조회 성공"));
    }

    @PatchMapping("/{order_id}/cancel")
    @Override
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
        @RequestHeader(value = "X-USER-ID") Integer userId,
        @PathVariable("order_id") Integer orderId,
        @RequestBody @Valid CancelOrderRequest request
    ) {
        CancelOrderResponse response = orderService.cancelOrder(userId, orderId, request.cancelReason());
        return ResponseEntity.ok(ApiResponse.success(response, "주문이 취소되었습니다."));
    }
}