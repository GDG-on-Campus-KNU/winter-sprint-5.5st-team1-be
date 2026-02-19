package com.gdg.sprint.team1.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.order.CancelOrderRequest;
import com.gdg.sprint.team1.dto.order.CancelOrderResponse;
import com.gdg.sprint.team1.dto.order.CreateOrderFromCartRequest;
import com.gdg.sprint.team1.dto.order.CreateOrderRequest;
import com.gdg.sprint.team1.dto.order.CreateOrderResponse;
import com.gdg.sprint.team1.dto.order.OrderDetailResponse;
import com.gdg.sprint.team1.dto.order.OrderResponse;
import com.gdg.sprint.team1.security.CurrentUser;
import com.gdg.sprint.team1.security.UserContextHolder;

@Tag(name = "주문 API", description = "주문 생성, 조회, 취소 (JWT 인증 필요)")
@SecurityRequirement(name = "bearerAuth")
public interface OrderApi {

    @Operation(summary = "주문 생성 (직접 입력)", description = "상품 목록 직접 입력. 재고 확인, 쿠폰 검증, 배송비(3만원 이상 무료), 재고 차감·쿠폰 사용 처리")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문 생성됨")
    })
    ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
        @Parameter(hidden = true) @CurrentUser UserContextHolder.UserContext user,
        @Valid CreateOrderRequest request
    );

    @Operation(summary = "주문 생성 (장바구니)", description = "장바구니 상품으로 주문. 장바구니 조회·재고 확인·주문 생성·장바구니 비우기")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문 생성됨")
    })
    ResponseEntity<ApiResponse<CreateOrderResponse>> createOrderFromCart(
        @Parameter(hidden = true) @CurrentUser UserContextHolder.UserContext user,
        @Valid CreateOrderFromCartRequest request
    );

    @Operation(summary = "주문 목록 조회", description = "내 주문 목록 페이징. 상태 필터(PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED) 지원")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(
        @Parameter(hidden = true) @CurrentUser UserContextHolder.UserContext user,
        @Parameter(description = "페이지 번호 (기본: 1)", example = "1") @Min(1) Integer page,
        @Parameter(description = "페이지당 항목 수 (기본: 10)", example = "10") @Min(1) @Max(100) Integer limit,
        @Parameter(description = "주문 상태", example = "PENDING", schema = @Schema(allowableValues = {"PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"})) String status
    );

    @Operation(summary = "주문 상세 조회", description = "특정 주문 상세. 본인 주문만 조회 가능")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 없음")
    })
    ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
        @Parameter(hidden = true) @CurrentUser UserContextHolder.UserContext user,
        @Positive Integer orderId
    );

    @Operation(summary = "주문 취소", description = "취소 시 재고·쿠폰 복구. PENDING·CONFIRMED만 취소 가능")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "취소 불가 상태")
    })
    ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
        @Parameter(hidden = true) @CurrentUser UserContextHolder.UserContext user,
        @Positive Integer orderId,
        @Valid CancelOrderRequest request
    );
}
