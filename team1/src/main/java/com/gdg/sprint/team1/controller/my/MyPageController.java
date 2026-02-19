package com.gdg.sprint.team1.controller.my;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.auth.UserMeResponse;
import com.gdg.sprint.team1.dto.my.MyCouponResponse;
import com.gdg.sprint.team1.service.my.MyPageService;

@Tag(name = "마이페이지 API", description = "내 정보·쿠폰 조회")
@RestController
@RequestMapping("/api/v1/my")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/info")
    @Operation(summary = "내 정보", description = "JWT에서 추출한 사용자 정보 반환 (인증 여부 확인용)",
        security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserMeResponse>> getMyInfo() {
        UserMeResponse data = myPageService.getMyInfo();
        return ResponseEntity.ok(ApiResponse.success(data, "조회 성공"));
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
