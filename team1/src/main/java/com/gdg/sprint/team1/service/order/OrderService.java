package com.gdg.sprint.team1.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdg.sprint.team1.domain.cart.CartItem;
import com.gdg.sprint.team1.dto.order.CancelOrderResponse;
import com.gdg.sprint.team1.dto.order.CreateOrderFromCartRequest;
import com.gdg.sprint.team1.dto.order.CreateOrderRequest;
import com.gdg.sprint.team1.dto.order.CreateOrderRequest.OrderItemRequest;
import com.gdg.sprint.team1.dto.order.CreateOrderResponse;
import com.gdg.sprint.team1.dto.order.OrderDetailResponse;
import com.gdg.sprint.team1.dto.order.OrderResponse;
import com.gdg.sprint.team1.entity.Coupon;
import com.gdg.sprint.team1.entity.Order;
import com.gdg.sprint.team1.entity.Order.OrderStatus;
import com.gdg.sprint.team1.entity.OrderItem;
import com.gdg.sprint.team1.entity.Product;
import com.gdg.sprint.team1.entity.User;
import com.gdg.sprint.team1.entity.UserCoupon;
import com.gdg.sprint.team1.exception.CannotCancelOrderException;
import com.gdg.sprint.team1.exception.CouponNotFoundException;
import com.gdg.sprint.team1.exception.EmptyCartException;
import com.gdg.sprint.team1.exception.EmptyOrderException;
import com.gdg.sprint.team1.exception.InsufficientStockException;
import com.gdg.sprint.team1.exception.InvalidCouponException;
import com.gdg.sprint.team1.exception.MinimumOrderNotMetException;
import com.gdg.sprint.team1.exception.OrderNotFoundException;
import com.gdg.sprint.team1.exception.ProductNotFoundException;
import com.gdg.sprint.team1.exception.UnauthorizedOrderAccessException;
import com.gdg.sprint.team1.exception.UserNotFoundException;
import com.gdg.sprint.team1.repository.CartItemRepository;
import com.gdg.sprint.team1.repository.OrderRepository;
import com.gdg.sprint.team1.repository.ProductRepository;
import com.gdg.sprint.team1.repository.UserCouponRepository;
import com.gdg.sprint.team1.repository.UserRepository;
import com.gdg.sprint.team1.service.pricing.CouponInfo;
import com.gdg.sprint.team1.service.pricing.CouponType;
import com.gdg.sprint.team1.service.pricing.PriceCalculationResult;
import com.gdg.sprint.team1.service.pricing.PriceCalculationService;
import com.gdg.sprint.team1.service.pricing.PriceItem;

/**
 * 주문 서비스
 *
 * 동시성 제어: Optimistic Lock (@Version)
 * - Product 엔티티에 version 필드 추가 필요
 * - 동시 주문 시 자동으로 충돌 감지 및 예외 발생
 * - 클라이언트에서 재시도 처리 권장
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;
    private final CartItemRepository cartItemRepository;
    private final PriceCalculationService priceCalculationService;

    public OrderService(OrderRepository orderRepository,
        ProductRepository productRepository,
        UserRepository userRepository,
        UserCouponRepository userCouponRepository,
        CartItemRepository cartItemRepository,
        PriceCalculationService priceCalculationService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.userCouponRepository = userCouponRepository;
        this.cartItemRepository = cartItemRepository;
        this.priceCalculationService = priceCalculationService;
    }

    /**
     * 주문 생성 (직접 상품 목록 입력)
     *
     * 동시성 제어:
     * - Product의 @Version 필드를 통한 Optimistic Lock
     * - 재고 차감 시 version 불일치 발생 시 ObjectOptimisticLockingFailureException 발생
     * - 클라이언트에서 에러 처리 및 재시도 필요
     */
    @Transactional
    public CreateOrderResponse createOrder(Integer userId, CreateOrderRequest request) {
        log.debug("주문 생성 시작: userId={}", userId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. 주문 항목 검증
        if (request.items() == null || request.items().isEmpty()) {
            throw new EmptyOrderException();
        }

        // 3. 상품 조회 및 재고 확인
        List<Integer> productIds = request.items().stream()
            .map(OrderItemRequest::productId)
            .toList();

        Map<Integer, Product> productMap = new HashMap<>();
        productRepository.findAllById(productIds.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList()))
            .forEach(product -> productMap.put(product.getId().intValue(), product));

        // 4. 각 상품 재고 확인 및 PriceItem 목록 생성
        List<PriceItem> priceItems = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest item : request.items()) {
            Product product = productMap.get(item.productId());
            if (product == null) {
                throw new ProductNotFoundException(item.productId().longValue());
            }

            // 재고 확인
            if (product.getStock() == null || product.getStock() < item.quantity()) {
                throw new InsufficientStockException(
                    product.getName(),
                    item.quantity(),
                    product.getStock() != null ? product.getStock() : 0
                );
            }

            priceItems.add(new PriceItem(product.getId(), product.getPrice(), item.quantity()));
        }

        // 5. 쿠폰 조회 및 검증
        UserCoupon userCoupon = null;
        CouponInfo couponInfo = null;
        if (request.userCouponId() != null) {
            userCoupon = userCouponRepository.findById(request.userCouponId())
                .orElseThrow(() -> new CouponNotFoundException(request.userCouponId()));

            // 쿠폰 사용 가능 여부 확인
            if (!userCoupon.isUsable()) {
                throw new InvalidCouponException("사용할 수 없는 쿠폰입니다.");
            }

            // 쿠폰 소유자 확인
            if (!userCoupon.getUser().getId().equals(userId)) {
                throw new InvalidCouponException("본인의 쿠폰만 사용할 수 있습니다.");
            }

            Coupon coupon = userCoupon.getCoupon();

            // 최소 주문 금액 체크
            BigDecimal totalProductPrice = priceItems.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (coupon.getMinOrderPrice() != null &&
                totalProductPrice.compareTo(coupon.getMinOrderPrice()) < 0) {
                throw new MinimumOrderNotMetException(totalProductPrice, coupon.getMinOrderPrice());
            }

            couponInfo = new CouponInfo(
                CouponType.valueOf(coupon.getCouponType().name()),
                coupon.getDiscountValue(),
                coupon.getMinOrderPrice()
            );
        }

        // 6. 금액 계산
        PriceCalculationResult priceResult = priceCalculationService.calculateTotal(priceItems, couponInfo);

        // 7. 주문 생성
        Order order = new Order();
        order.setUser(user);
        order.setUserCoupon(userCoupon);
        order.setTotalProductPrice(priceResult.totalProductPrice());
        order.setDiscountAmount(priceResult.discountAmount());
        order.setDeliveryFee(priceResult.deliveryFee());
        order.setFinalPrice(priceResult.finalPrice());
        order.setRecipientName(request.recipientName());
        order.setRecipientPhone(request.recipientPhone());
        order.setDeliveryAddress(request.deliveryAddress());
        order.setDeliveryDetailAddress(request.deliveryDetailAddress());
        order.setDeliveryMessage(request.deliveryMessage());
        order.setOrderStatus(OrderStatus.PENDING);

        orderRepository.save(order);

        // 8. OrderItem 저장 및 재고 차감
        for (CreateOrderRequest.OrderItemRequest item : request.items()) {
            Product product = productMap.get(item.productId());

            OrderItem orderItem = new OrderItem(order, product, item.quantity(), product.getPrice());
            order.addOrderItem(orderItem);

            // 재고 차감 (Optimistic Lock으로 동시성 제어)
            // Product의 @Version 필드가 자동으로 증가하며 충돌 감지
            product.setStock(product.getStock() - item.quantity());
        }

        // 9. 쿠폰 사용 처리
        if (userCoupon != null) {
            userCoupon.use();
        }

        log.info("주문 생성 완료: orderId={}, userId={}, finalPrice={}",
            order.getId(), userId, order.getFinalPrice());

        return CreateOrderResponse.from(order);
    }

    /**
     * 장바구니 기반 주문 생성
     *
     * 동시성 제어: Optimistic Lock 적용
     */
    @Transactional
    public CreateOrderResponse createOrderFromCart(Integer userId, CreateOrderFromCartRequest request) {
        log.debug("장바구니 기반 주문 생성 시작: userId={}", userId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. 장바구니 조회
        List<CartItem> cartItems = cartItemRepository.findAllByIdUserId(userId);
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        // 3. 상품 조회 및 재고 확인
        List<Integer> productIds = cartItems.stream()
            .map(item -> item.getId().getProductId())
            .collect(Collectors.toList());

        Map<Integer, Product> productMap = new HashMap<>();
        productRepository.findAllById(productIds.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList()))
            .forEach(product -> productMap.put(product.getId().intValue(), product));

        // 4. 재고 확인 및 PriceItem 목록 생성
        List<PriceItem> priceItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Integer productId = cartItem.getId().getProductId();
            Product product = productMap.get(productId);

            if (product == null) {
                throw new ProductNotFoundException(productId.longValue());
            }

            // 재고 확인
            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                    product.getName(),
                    cartItem.getQuantity(),
                    product.getStock() != null ? product.getStock() : 0
                );
            }

            priceItems.add(new PriceItem(product.getId(), product.getPrice(), cartItem.getQuantity()));
        }

        // 5. 쿠폰 조회 및 검증
        UserCoupon userCoupon = null;
        CouponInfo couponInfo = null;
        if (request.userCouponId() != null) {
            userCoupon = userCouponRepository.findById(request.userCouponId())
                .orElseThrow(() -> new CouponNotFoundException(request.userCouponId()));

            if (!userCoupon.isUsable()) {
                throw new InvalidCouponException("사용할 수 없는 쿠폰입니다.");
            }

            if (!userCoupon.getUser().getId().equals(userId)) {
                throw new InvalidCouponException("본인의 쿠폰만 사용할 수 있습니다.");
            }

            Coupon coupon = userCoupon.getCoupon();

            // 최소 주문 금액 체크
            BigDecimal totalProductPrice = priceItems.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (coupon.getMinOrderPrice() != null &&
                totalProductPrice.compareTo(coupon.getMinOrderPrice()) < 0) {
                throw new MinimumOrderNotMetException(totalProductPrice, coupon.getMinOrderPrice());
            }

            couponInfo = new CouponInfo(
                CouponType.valueOf(coupon.getCouponType().name()),
                coupon.getDiscountValue(),
                coupon.getMinOrderPrice()
            );
        }

        // 6. 금액 계산
        PriceCalculationResult priceResult = priceCalculationService.calculateTotal(priceItems, couponInfo);

        // 7. 주문 생성
        Order order = new Order();
        order.setUser(user);
        order.setUserCoupon(userCoupon);
        order.setTotalProductPrice(priceResult.totalProductPrice());
        order.setDiscountAmount(priceResult.discountAmount());
        order.setDeliveryFee(priceResult.deliveryFee());
        order.setFinalPrice(priceResult.finalPrice());
        order.setRecipientName(request.recipientName());
        order.setRecipientPhone(request.recipientPhone());
        order.setDeliveryAddress(request.deliveryAddress());
        order.setDeliveryDetailAddress(request.deliveryDetailAddress());
        order.setDeliveryMessage(request.deliveryMessage());
        order.setOrderStatus(OrderStatus.PENDING);

        orderRepository.save(order);

        // 8. OrderItem 저장 및 재고 차감
        for (CartItem cartItem : cartItems) {
            Product product = productMap.get(cartItem.getId().getProductId());

            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity(), product.getPrice());
            order.addOrderItem(orderItem);

            // 재고 차감 (Optimistic Lock으로 동시성 제어)
            product.setStock(product.getStock() - cartItem.getQuantity());
        }

        // 9. 쿠폰 사용 처리
        if (userCoupon != null) {
            userCoupon.use();
        }

        // 10. 장바구니 비우기
        cartItemRepository.deleteByUserIdAndProductIds(userId, productIds);

        log.info("장바구니 기반 주문 생성 완료: orderId={}, userId={}, finalPrice={}",
            order.getId(), userId, order.getFinalPrice());

        return CreateOrderResponse.from(order);
    }

    /**
     * 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Integer userId, Integer page, Integer limit, String status) {
        // 페이지네이션 상한선 적용 (최대 100개)
        int safePage = page != null && page >= 1 ? page : 1;
        int safeLimit = limit != null && limit >= 1 ? Math.min(limit, 100) : 10;

        log.debug("주문 목록 조회: userId={}, page={}, limit={}", userId, safePage, safeLimit);

        PageRequest pageable = PageRequest.of(
            safePage - 1,
            safeLimit,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Order> orderPage;
        if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orderPage = orderRepository.findAllByUserIdAndOrderStatus(userId, orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("유효하지 않은 주문 상태: {}, 전체 조회로 대체", status);
                orderPage = orderRepository.findAllByUserId(userId, pageable);
            }
        } else {
            orderPage = orderRepository.findAllByUserId(userId, pageable);
        }

        return orderPage.map(OrderResponse::from);
    }

    /**
     * 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Integer userId, Integer orderId) {
        log.debug("주문 상세 조회: userId={}, orderId={}", userId, orderId);

        Order order = orderRepository.findByIdWithDetails(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 본인의 주문만 조회 가능
        if (!order.getUser().getId().equals(userId)) {
            log.warn("권한 없는 주문 조회 시도: userId={}, orderId={}", userId, orderId);
            throw new UnauthorizedOrderAccessException();
        }

        return OrderDetailResponse.from(order);
    }

    /**
     * 주문 취소
     *
     * 비즈니스 로직:
     * 1. 주문 조회 및 권한 확인
     * 2. 취소 가능 상태 검증 (PENDING, CONFIRMED만 가능)
     * 3. 재고 복구 (각 주문 아이템의 quantity만큼 Product.stock 증가)
     * 4. 쿠폰 복구 (UserCoupon.used_at을 null로 변경)
     * 5. 주문 상태를 CANCELLED로 변경
     * 6. 취소 사유 저장
     */
    @Transactional
    public CancelOrderResponse cancelOrder(Integer userId, Integer orderId, String cancelReason) {
        log.debug("주문 취소 시작: userId={}, orderId={}", userId, orderId);

        // 1. 주문 조회 (JOIN FETCH로 OrderItem, UserCoupon 함께 조회)
        Order order = orderRepository.findByIdWithDetails(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2. 본인 확인
        if (!order.getUser().getId().equals(userId)) {
            log.warn("권한 없는 주문 취소 시도: userId={}, orderId={}", userId, orderId);
            throw new UnauthorizedOrderAccessException();
        }

        // 3. 취소 가능 상태 확인 (PENDING, CONFIRMED만 가능)
        OrderStatus currentStatus = order.getOrderStatus();
        if (currentStatus != OrderStatus.PENDING && currentStatus != OrderStatus.CONFIRMED) {
            log.warn("취소 불가 상태: orderId={}, status={}", orderId, currentStatus);
            throw new CannotCancelOrderException(currentStatus);
        }

        // 4. 재고 복구
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            Integer currentStock = product.getStock();
            Integer returnQuantity = orderItem.getQuantity();
            product.setStock(currentStock + returnQuantity);
            log.debug("재고 복구: productId={}, 복구량={}, 새 재고={}",
                product.getId(), returnQuantity, product.getStock());
        }

        // 5. 쿠폰 복구
        UserCoupon userCoupon = order.getUserCoupon();
        if (userCoupon != null) {
            userCoupon.setUsedAt(null);
            log.debug("쿠폰 복구: userCouponId={}", userCoupon.getId());
        }

        // 6. 주문 상태 변경 및 취소 사유 저장
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelReason(cancelReason);

        log.info("주문 취소 완료: orderId={}, userId={}, 환불액={}",
            orderId, userId, order.getFinalPrice());

        return CancelOrderResponse.from(order);
    }
}