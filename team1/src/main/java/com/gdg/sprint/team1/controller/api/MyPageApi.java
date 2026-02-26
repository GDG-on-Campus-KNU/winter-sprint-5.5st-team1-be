package com.gdg.sprint.team1.controller.api;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.auth.UserMeResponse;
import com.gdg.sprint.team1.dto.my.MyCouponResponse;
import com.gdg.sprint.team1.dto.order.OrderDetailResponse;
import com.gdg.sprint.team1.dto.order.OrderResponse;
import com.gdg.sprint.team1.security.CurrentUser;
import com.gdg.sprint.team1.security.UserContextHolder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "마이페이지 API", description = "내 정보·쿠폰 조회 (JWT 인증 필요)")
public interface MyPageApi {

    @Operation(summary = "내 정보", description = "JWT에서 추출한 사용자 정보 반환", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<UserMeResponse>> getMyInfo(
        @Parameter(hidden = true) @CurrentUser UserContextHolder.UserContext user
    );

    @Operation(summary = "내 쿠폰 목록", description = "사용 가능/사용 완료 필터 (status=AVAILABLE | USED | EXPIRED, 미지정 시 전체)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<List<MyCouponResponse>>> getMyCoupons(
        @Parameter(hidden = true) @CurrentUser UserContextHolder.UserContext user,
        @Parameter(description = "AVAILABLE: 사용가능, USED: 사용완료, EXPIRED: 만료", schema = @Schema(allowableValues = {"AVAILABLE", "USED", "EXPIRED"})) String status
    );

    @Operation(
            summary = "내 주문 목록",
            description = "기간(1/3/6개월) + 상태 필터로 내 주문 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @Parameter(hidden = true) @CurrentUser UserContextHolder.UserContext user,
            @Parameter(description = "페이지 번호 (기본: 1)", example = "1") @Min(1) Integer page,
            @Parameter(description = "페이지당 항목 수 (기본: 10)", example = "10") @Min(1) @Max(100) Integer limit,
            @Parameter(description = "주문 상태", schema = @Schema(
                    allowableValues = {"PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"}
            )) String status,
            @Parameter(description = "조회 기간(개월)", schema = @Schema(allowableValues = {"1", "3", "6"})) Integer months
    );

    @Operation(
            summary = "내 주문 상세",
            description = "내 주문 상세 정보(배송 정보 포함)를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 없음")
    })
    ResponseEntity<ApiResponse<OrderDetailResponse>> getMyOrderDetail(
            @Parameter(hidden = true) @CurrentUser UserContextHolder.UserContext user,
            @Parameter(description = "주문 ID", example = "1") @Positive Integer orderId
    );
}
