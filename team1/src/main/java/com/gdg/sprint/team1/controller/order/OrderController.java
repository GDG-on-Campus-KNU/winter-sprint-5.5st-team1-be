package com.gdg.sprint.team1.controller.order;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import com.gdg.sprint.team1.service.order.OrderService;

@Tag(
    name = "주문 API",
    description = "주문 생성, 조회, 취소 등 주문 관련 모든 기능을 제공합니다. (JWT 인증 필요)"
)
@RestController
@RequestMapping("/api/v1/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(
        summary = "주문 생성 (직접 입력)",
        description = """
            ### 상품 목록을 직접 입력하여 주문을 생성합니다.
            
            **처리 흐름:**
            1. 상품 재고 확인
            2. 쿠폰 유효성 검증 (있는 경우)
            3. 최소 주문 금액 체크 (쿠폰 사용 시)
            4. 배송비 계산 (3만원 이상 무료)
            5. 주문 생성 및 재고 차감
            6. 쿠폰 사용 처리
            
            **주의사항:**
            - 재고가 부족하면 주문이 실패합니다.
            - 쿠폰 사용 시 최소 주문 금액을 충족해야 합니다.
            - 주문 생성 시 재고가 자동으로 차감됩니다.
            """,
        parameters = {}
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "✅ 주문 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateOrderResponse.class),
                examples = @ExampleObject(
                    name = "성공 예시",
                    value = """
                        {
                          "success": true,
                          "data": {
                            "order": {
                              "id": 101,
                              "user_id": 1,
                              "user_coupon_id": 5,
                              "order_status": "PENDING",
                              "delivery_address": "서울특별시 강남구 테헤란로 123",
                              "total_product_price": 28000.00,
                              "discount_amount": 3000.00,
                              "delivery_fee": 3000.00,
                              "final_price": 28000.00,
                              "created_at": "2024-02-08T11:00:00Z"
                            },
                            "order_items": [
                              {
                                "product_id": 1,
                                "product_name": "김치찌개",
                                "quantity": 2,
                                "unit_price": 9000.00,
                                "subtotal": 18000.00
                              }
                            ],
                            "coupon_applied": {
                              "coupon_id": 2,
                              "coupon_name": "신규가입 할인",
                              "discount_amount": 3000.00
                            }
                          },
                          "message": "주문이 생성되었습니다.",
                          "timestamp": "2024-02-08T11:00:00Z"
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "❌ 잘못된 요청 (재고 부족, 최소 주문 금액 미달 등)",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "재고 부족",
                        value = """
                            {
                              "success": false,
                              "error": {
                                "code": "OUT_OF_STOCK",
                                "message": "김치찌개의 재고가 부족합니다. (요청: 5개, 재고: 3개)"
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "최소 주문 금액 미달",
                        value = """
                            {
                              "success": false,
                              "error": {
                                "code": "MINIMUM_ORDER_NOT_MET",
                                "message": "최소 주문 금액을 충족하지 못했습니다. (현재: 15000원, 최소: 20000원)"
                              }
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "❌ 리소스를 찾을 수 없음 (상품, 쿠폰 등)",
            content = @Content(
                examples = @ExampleObject(
                    name = "상품 없음",
                    value = """
                        {
                          "success": false,
                          "error": {
                            "code": "PRODUCT_NOT_FOUND",
                            "message": "존재하지 않는 상품입니다."
                          }
                        }
                        """
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "주문 생성 요청 정보",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CreateOrderRequest.class),
                examples = @ExampleObject(
                    name = "주문 생성 예시",
                    value = """
                            {
                              "items": [
                                {
                                  "product_id": 1,
                                  "quantity": 2
                                },
                                {
                                  "product_id": 3,
                                  "quantity": 1
                                }
                              ],
                              "user_coupon_id": 5,
                              "recipient_name": "홍길동",
                              "recipient_phone": "010-1234-5678",
                              "delivery_address": "서울특별시 강남구 테헤란로 123",
                              "delivery_detail_address": "456호",
                              "delivery_message": "문 앞에 놓아주세요"
                            }
                            """
                )
            )
        )
        @Validated @RequestBody CreateOrderRequest request) {

        CreateOrderResponse response = orderService.createOrder(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "주문이 생성되었습니다."));
    }

    @Operation(
        summary = "주문 생성 (장바구니 기반)",
        description = """
            ### 장바구니에 담긴 상품으로 주문을 생성합니다.
            
            **처리 흐름:**
            1. 장바구니 조회
            2. 상품 재고 확인
            3. 쿠폰 유효성 검증 (있는 경우)
            4. 주문 생성
            5. 재고 차감
            6. 장바구니 비우기
            
            **장점:**
            - 상품 목록을 직접 입력할 필요 없음
            - 장바구니에 담긴 모든 상품을 한 번에 주문
            - 주문 완료 시 장바구니 자동 삭제
            """,
        parameters = {}
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "✅ 주문 생성 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "❌ 장바구니가 비어있거나 재고 부족",
            content = @Content(
                examples = @ExampleObject(
                    name = "장바구니 비어있음",
                    value = """
                        {
                          "success": false,
                          "error": {
                            "code": "EMPTY_CART",
                            "message": "장바구니가 비어있습니다."
                          }
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrderFromCart(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "배송지 정보만 입력 (상품은 장바구니에서 자동 조회)",
            required = true,
            content = @Content(
                examples = @ExampleObject(
                    name = "장바구니 주문 예시",
                    value = """
                            {
                              "user_coupon_id": 5,
                              "recipient_name": "홍길동",
                              "recipient_phone": "010-1234-5678",
                              "delivery_address": "서울특별시 강남구 테헤란로 123",
                              "delivery_detail_address": "456호",
                              "delivery_message": "문 앞에 놓아주세요"
                            }
                            """
                )
            )
        )
        @Validated @RequestBody CreateOrderFromCartRequest request) {

        CreateOrderResponse response = orderService.createOrderFromCart(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "주문이 생성되었습니다."));
    }

    @Operation(
        summary = "주문 목록 조회",
        description = """
            ### 내 주문 목록을 조회합니다 (페이징 지원).
            
            **기능:**
            - 최신 주문부터 정렬
            - 주문 상태별 필터링 가능
            - 페이지당 10개 기본 표시
            
            **주문 상태:**
            - PENDING: 주문 대기
            - CONFIRMED: 주문 확정
            - SHIPPING: 배송 중
            - DELIVERED: 배송 완료
            - CANCELLED: 주문 취소
            """,
        parameters = {
            @Parameter(
                name = "page",
                description = "페이지 번호 (1부터 시작)",
                example = "1",
                schema = @Schema(type = "integer", defaultValue = "1")
            ),
            @Parameter(
                name = "limit",
                description = "페이지당 항목 수",
                example = "10",
                schema = @Schema(type = "integer", defaultValue = "10")
            ),
            @Parameter(
                name = "status",
                description = "주문 상태 필터 (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)",
                example = "PENDING",
                schema = @Schema(type = "string", allowableValues = {"PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"})
            )
        }
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "✅ 조회 성공",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "data": {
                            "content": [
                              {
                                "id": 101,
                                "order_status": "PENDING",
                                "total_product_price": 28000.00,
                                "discount_amount": 3000.00,
                                "delivery_fee": 3000.00,
                                "final_price": 28000.00,
                                "delivery_address": "서울특별시 강남구 테헤란로 123",
                                "created_at": "2024-02-08T11:00:00Z",
                                "item_count": 2
                              }
                            ],
                            "pageable": {
                              "pageNumber": 0,
                              "pageSize": 10
                            },
                            "totalPages": 5,
                            "totalElements": 48
                          },
                          "message": "주문 목록 조회 성공"
                        }
                        """
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(
        @Parameter(description = "페이지 번호 (기본값: 1)", example = "1")
        @RequestParam(required = false, defaultValue = "1") Integer page,  // ← 기본값 1

        @Parameter(description = "페이지당 항목 수 (기본값: 10)", example = "10")
        @RequestParam(required = false, defaultValue = "10") Integer limit,  // ← 기본값 10

        @Parameter(description = "주문 상태 필터", example = "PENDING")
        @RequestParam(required = false) String status
    ) {
        Page<OrderResponse> response = orderService.getOrders(page, limit, status);
        return ResponseEntity.ok(ApiResponse.success(response, "주문 목록 조회 성공"));
    }

    @Operation(
        summary = "주문 상세 조회",
        description = """
            ### 특정 주문의 상세 정보를 조회합니다.
            
            **포함 정보:**
            - 주문 기본 정보 (금액, 배송지, 상태 등)
            - 주문 상품 목록 (상품명, 수량, 가격 등)
            - 사용된 쿠폰 정보 (있는 경우)
            
            **권한:**
            - 본인의 주문만 조회 가능
            - 타인의 주문 조회 시 403 Forbidden
            """,
        parameters = {
            @Parameter(
                name = "order_id",
                description = "주문 ID",
                required = true,
                example = "101",
                schema = @Schema(type = "integer")
            )
        }
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "✅ 조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "❌ 권한 없음 (타인의 주문)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "error": {
                            "code": "UNAUTHORIZED_ACCESS",
                            "message": "본인의 주문만 조회할 수 있습니다."
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "❌ 주문을 찾을 수 없음"
        )
    })
    @GetMapping("/{order_id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
        @PathVariable("order_id") Integer orderId) {

        OrderDetailResponse response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "주문 상세 조회 성공"));
    }

    @Operation(
        summary = "주문 취소",
        description = """
            ### 주문을 취소하고 재고와 쿠폰을 복구합니다.
            
            **처리 내용:**
            1. 주문 상태를 CANCELLED로 변경
            2. 재고 복구 (주문한 수량만큼 Product.stock 증가)
            3. 쿠폰 복구 (used_at을 null로 변경, 다시 사용 가능)
            4. 취소 사유 저장
            
            **취소 가능 조건:**
            - ✅ PENDING (주문 대기) - 취소 가능
            - ✅ CONFIRMED (주문 확정) - 취소 가능
            - ❌ SHIPPING (배송 중) - 취소 불가
            - ❌ DELIVERED (배송 완료) - 취소 불가
            - ❌ CANCELLED (이미 취소됨) - 취소 불가
            
            **주의사항:**
            - 배송이 시작된 후에는 취소할 수 없습니다.
            - 취소 시 재고와 쿠폰이 자동으로 복구됩니다.
            - 본인의 주문만 취소 가능합니다.
            """,
        parameters = {
            @Parameter(
                name = "order_id",
                description = "취소할 주문 ID",
                required = true,
                example = "101",
                schema = @Schema(type = "integer")
            )
        }
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "✅ 취소 성공",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "data": {
                            "order_id": 101,
                            "order_status": "CANCELLED",
                            "cancel_reason": "단순 변심",
                            "cancelled_at": "2024-02-08T11:10:00Z",
                            "refund_amount": 28000.00
                          },
                          "message": "주문이 취소되었습니다."
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "❌ 취소 불가 (배송 중, 배송 완료 등)",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "배송 중",
                        value = """
                            {
                              "success": false,
                              "error": {
                                "code": "CANNOT_CANCEL_ORDER",
                                "message": "배송 중인 주문은 취소할 수 없습니다."
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "배송 완료",
                        value = """
                            {
                              "success": false,
                              "error": {
                                "code": "CANNOT_CANCEL_ORDER",
                                "message": "배송 완료된 주문은 취소할 수 없습니다."
                              }
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "❌ 권한 없음 (타인의 주문)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "❌ 주문을 찾을 수 없음"
        )
    })
    @PatchMapping("/{order_id}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
        @PathVariable("order_id") Integer orderId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "취소 사유 (선택)",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                            {
                              "cancel_reason": "단순 변심"
                            }
                            """
                )
            )
        )
        @Validated @RequestBody CancelOrderRequest request) {

        CancelOrderResponse response = orderService.cancelOrder(
            orderId,
            request.cancelReason()
        );
        return ResponseEntity.ok(ApiResponse.success(response, "주문이 취소되었습니다."));
    }
}