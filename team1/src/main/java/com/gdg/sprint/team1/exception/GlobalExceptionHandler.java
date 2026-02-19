package com.gdg.sprint.team1.exception;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gdg.sprint.team1.common.ApiResponse;
import com.gdg.sprint.team1.common.ApiResponse.FieldErrorEntry;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ===== 상품 관련 예외 =====

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFound(ProductNotFoundException ex) {
        log.warn("상품을 찾을 수 없음: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure("PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."));
    }

    // ===== 사용자 관련 예외 =====

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure("USER_NOT_FOUND", "존재하지 않는 사용자입니다."));
    }

    // ===== 인증/권한 예외 =====

    @ExceptionHandler(AuthRequiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthRequired(AuthRequiredException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.failure("AUTH_REQUIRED", "인증이 필요합니다."));
    }

    @ExceptionHandler(AuthExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthExpired(AuthExpiredException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.failure("AUTH_EXPIRED", "토큰이 만료되었습니다."));
    }

    @ExceptionHandler(AuthInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthInvalid(AuthInvalidException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.failure("AUTH_INVALID", "유효하지 않은 토큰입니다."));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.failure("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.failure("DUPLICATE_EMAIL", ex.getMessage()));
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLoginFailed(LoginFailedException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.failure("LOGIN_FAILED", ex.getMessage()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.failure("REFRESH_TOKEN_INVALID", ex.getMessage()));
    }

    // ===== 주문 관련 예외 =====

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderNotFound(OrderNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure("ORDER_NOT_FOUND", "존재하지 않는 주문입니다."));
    }

    @ExceptionHandler(EmptyOrderException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmptyOrder(EmptyOrderException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("EMPTY_ORDER", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedOrderAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedOrderAccess(UnauthorizedOrderAccessException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.failure("UNAUTHORIZED_ACCESS", ex.getMessage()));
    }

    /**
     * 주문 취소 불가 예외 처리
     * API 명세: CANNOT_CANCEL_ORDER (400)
     */
    @ExceptionHandler(CannotCancelOrderException.class)
    public ResponseEntity<ApiResponse<Void>> handleCannotCancelOrder(CannotCancelOrderException ex) {
        log.warn("주문 취소 불가: {} (상태: {})", ex.getMessage(), ex.getCurrentStatus());

        String message = String.format("%s (현재 상태: %s)",
            ex.getMessage(),
            ex.getCurrentStatus().name());

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("CANNOT_CANCEL_ORDER", message));
    }

    /**
     * 최소 주문 금액 미달 예외 처리
     * API 명세: MINIMUM_ORDER_NOT_MET (400)
     */
    @ExceptionHandler(MinimumOrderNotMetException.class)
    public ResponseEntity<ApiResponse<Void>> handleMinimumOrderNotMet(MinimumOrderNotMetException ex) {
        log.warn("최소 주문 금액 미달: 현재={}원, 최소={}원",
            ex.getCurrentAmount(), ex.getMinimumRequired());

        String message = String.format(
            "최소 주문 금액을 충족하지 못했습니다. (현재: %s원, 최소: %s원)",
            ex.getCurrentAmount(),
            ex.getMinimumRequired()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("MINIMUM_ORDER_NOT_MET", message));
    }

    // ===== 재고 관련 예외 =====

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("재고 부족: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("OUT_OF_STOCK", ex.getMessage()));
    }

    // ===== 쿠폰 관련 예외 =====

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCouponNotFound(CouponNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure("COUPON_NOT_FOUND", "존재하지 않는 쿠폰입니다."));
    }

    @ExceptionHandler(InvalidCouponException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCoupon(InvalidCouponException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("INVALID_COUPON", ex.getMessage()));
    }

    // ===== 장바구니 관련 예외 =====

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmptyCart(EmptyCartException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("EMPTY_CART", ex.getMessage()));
    }

    /**
     * 이미 장바구니에 있는 상품 예외 처리
     * API 명세: ALREADY_IN_CART (409)
     *
     * 주의: API 명세에는 409 Conflict로 명시되어 있으나,
     * 실제로는 수량 증가 처리하고 200 OK로 응답하는 것을 권장합니다.
     * 이 핸들러는 명세 준수를 위해 남겨둡니다.
     */
    @ExceptionHandler(AlreadyInCartException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyInCart(AlreadyInCartException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.failure("ALREADY_IN_CART", ex.getMessage()));
    }

    /**
     * 장바구니에 상품이 없음 예외 처리
     * API 명세: CART_ITEM_NOT_FOUND (404)
     */
    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCartItemNotFound(CartItemNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.failure("CART_ITEM_NOT_FOUND", ex.getMessage()));
    }

    // ===== 공통 예외 =====

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorEntry> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> new FieldErrorEntry(err.getField(), err.getDefaultMessage()))
            .collect(Collectors.toList());
        String message = fieldErrors.isEmpty()
            ? "요청 값이 올바르지 않습니다."
            : fieldErrors.get(0).field() + ": " + fieldErrors.get(0).message();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("INVALID_REQUEST", message, fieldErrors));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("MISSING_HEADER", ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
            .findFirst()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .orElse("요청 값이 올바르지 않습니다.");
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("INVALID_REQUEST", message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("데이터 제약 위반: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("DATA_INTEGRITY_VIOLATION", "요청 처리 중 제약 조건 위반이 발생했습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("잘못된 인자: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure("INVALID_ARGUMENT", ex.getMessage()));
    }

    /**
     * Optimistic Lock 충돌 (동시성 문제)
     *
     * 발생 상황:
     * - 동시에 같은 상품의 재고를 차감할 때
     * - Product의 version 필드 불일치
     *
     * 클라이언트 처리:
     * - 재시도 (Retry) 권장
     * - "주문이 몰려 처리가 지연되고 있습니다. 다시 시도해주세요" 안내
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("동시성 충돌 발생 (Optimistic Lock): {}", ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.CONFLICT)  // 409 Conflict
            .body(ApiResponse.failure(
                "CONCURRENT_UPDATE_CONFLICT",
                "동시에 여러 요청이 발생하여 처리할 수 없습니다. 잠시 후 다시 시도해주세요."
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("예상치 못한 서버 오류 발생", ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.failure("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}