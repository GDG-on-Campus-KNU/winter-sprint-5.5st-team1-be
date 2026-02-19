package com.gdg.sprint.team1.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.dto.auth.UserMeResponse;
import com.gdg.sprint.team1.exception.AuthRequiredException;
import com.gdg.sprint.team1.security.UserContextHolder;
import com.gdg.sprint.team1.service.auth.AuthService;

@Tag(name = "사용자 API", description = "내 정보 조회")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보", description = "JWT에서 추출한 사용자 정보 반환 (인증 여부 확인용)",
        security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserMeResponse>> me() {
        Integer userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new AuthRequiredException();
        }
        UserMeResponse response = UserMeResponse.from(authService.getCurrentUser(userId));
        return ResponseEntity.ok(ApiResponse.success(response, "조회 성공"));
    }
}
