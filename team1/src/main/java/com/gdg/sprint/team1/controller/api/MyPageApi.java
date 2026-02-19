package com.gdg.sprint.team1.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.auth.UserMeResponse;
import com.gdg.sprint.team1.dto.my.MyCouponResponse;

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
    ResponseEntity<ApiResponse<UserMeResponse>> getMyInfo();

    @Operation(summary = "내 쿠폰 목록", description = "사용 가능/사용 완료 필터 (status=AVAILABLE | USED, 미지정 시 전체)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<List<MyCouponResponse>>> getMyCoupons(
        @Parameter(description = "AVAILABLE: 사용가능, USED: 사용완료", schema = @Schema(allowableValues = {"AVAILABLE", "USED"})) String status
    );
}
