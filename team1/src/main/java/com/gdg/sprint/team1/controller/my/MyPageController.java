package com.gdg.sprint.team1.controller.my;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.my.MyCouponResponse;
import com.gdg.sprint.team1.dto.order.OrderDetailResponse;
import com.gdg.sprint.team1.dto.order.OrderResponse;
import com.gdg.sprint.team1.service.my.MyPageService;

@Tag(name = "마이페이지 API", description = "내 주문/쿠폰 조회")
@RestController
@RequestMapping("/api/v1/my")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping("/orders")
    @Operation(summary = "내 주문 목록", description = "최신순 주문 목록 (대표 상품명/총액/상태 포함)",
        security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
        @Parameter(description = "페이지 번호", example = "1") @RequestParam(required = false, defaultValue = "1") Integer page,
        @Parameter(description = "페이지당 개수", example = "10") @RequestParam(required = false, defaultValue = "10") Integer limit,
        @Parameter(description = "주문 상태 필터", schema = @Schema(allowableValues = {"PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"}))
        @RequestParam(required = false) String status
    ) {
        Page<OrderResponse> data = myPageService.getMyOrders(page, limit, status);
        return ResponseEntity.ok(ApiResponse.success(data, "주문 목록 조회 성공"));
    }

    @GetMapping("/orders/{order_id}")
    @Operation(summary = "내 주문 상세", description = "주문 상세, 배송지, 상태",
        security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getMyOrderDetail(
        @Parameter(description = "주문 ID") @PathVariable("order_id") Integer orderId
    ) {
        OrderDetailResponse data = myPageService.getMyOrderDetail(orderId);
        return ResponseEntity.ok(ApiResponse.success(data, "주문 상세 조회 성공"));
    }

    @GetMapping("/coupons")
    @Operation(summary = "내 쿠폰 목록", description = "사용 가능/사용 완료 필터 (status=AVAILABLE | USED, 미지정 시 전체)",
        security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<MyCouponResponse>>> getMyCoupons(
        @Parameter(description = "AVAILABLE: 사용가능, USED: 사용완료", schema = @Schema(allowableValues = {"AVAILABLE", "USED"}))
        @RequestParam(required = false) String status
    ) {
        List<MyCouponResponse> data = myPageService.getMyCoupons(status);
        return ResponseEntity.ok(ApiResponse.success(data, "쿠폰 목록 조회 성공"));
    }
}
