package com.gdg.sprint.team1.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdg.sprint.team1.domain.cart.CartItem;
import com.gdg.sprint.team1.dto.order.*;
import com.gdg.sprint.team1.entity.*;
import com.gdg.sprint.team1.entity.Order.OrderStatus;
import com.gdg.sprint.team1.exception.*;
import com.gdg.sprint.team1.repository.*;
import com.gdg.sprint.team1.service.pricing.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserCouponRepository userCouponRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final PriceCalculationService priceCalculationService;

    /**
     * 주문 생성 (제품 상세페이지 내 주문)
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
        List<PriceItem> priceItems = createAndValidatePriceItems(request.items());

        // 4. 쿠폰 검증
        UserCoupon userCoupon = validateCoupon(userId, request.userCouponId(), priceItems);
        CouponInfo couponInfo = userCoupon != null ? extractCouponInfo(userCoupon.getCoupon()) : null;

        // 5. 금액 계산
        PriceCalculationResult priceResult = priceCalculationService.calculateTotal(priceItems, couponInfo);

        // 6. 주문 생성
        Order order = Order.builder()
            .user(user)
            .userCoupon(userCoupon)
            .totalProductPrice(priceResult.totalProductPrice())
            .discountAmount(priceResult.discountAmount())
            .deliveryFee(priceResult.deliveryFee())
            .finalPrice(priceResult.finalPrice())
            .recipientName(request.recipientName())
            .recipientPhone(request.recipientPhone())
            .deliveryAddress(request.deliveryAddress())
            .deliveryDetailAddress(request.deliveryDetailAddress())
            .deliveryMessage(request.deliveryMessage())
            // orderStatus는 자동으로 PENDING (@Builder.Default)
            .build();

        orderRepository.save(order);

        // 7. OrderItem 생성 및 재고 차감
        createOrderItemsAndDeductStock(order, request.items());

        // 8. 쿠폰 사용
        if (userCoupon != null) {
            userCoupon.use();
        }

        log.info("주문 생성 완료: orderId={}, userId={}, finalPrice={}",
            order.getId(), userId, order.getFinalPrice());

        return CreateOrderResponse.from(order);
    }

    /**
     * 장바구니 기반 주문 생성
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

        // 3. 장바구니 기반 PriceItem 생성 및 재고 확인
        List<PriceItem> priceItems = createAndValidatePriceItemsFromCart(cartItems);

        // 4. 쿠폰 검증
        UserCoupon userCoupon = validateCoupon(userId, request.userCouponId(), priceItems);
        CouponInfo couponInfo = userCoupon != null ? extractCouponInfo(userCoupon.getCoupon()) : null;

        // 5. 금액 계산
        PriceCalculationResult priceResult = priceCalculationService.calculateTotal(priceItems, couponInfo);

        // 6. 주문 생성 (✅ Builder 패턴)
        Order order = Order.builder()
            .user(user)
            .userCoupon(userCoupon)
            .totalProductPrice(priceResult.totalProductPrice())
            .discountAmount(priceResult.discountAmount())
            .deliveryFee(priceResult.deliveryFee())
            .finalPrice(priceResult.finalPrice())
            .recipientName(request.recipientName())
            .recipientPhone(request.recipientPhone())
            .deliveryAddress(request.deliveryAddress())
            .deliveryDetailAddress(request.deliveryDetailAddress())
            .deliveryMessage(request.deliveryMessage())
            .build();

        orderRepository.save(order);

        // 7. OrderItem 생성 및 재고 차감
        createOrderItemsFromCartAndDeductStock(order, cartItems);

        // 8. 쿠폰 사용
        if (userCoupon != null) {
            userCoupon.use();
        }

        // 9. 장바구니 비우기
        List<Integer> productIds = cartItems.stream()
            .map(item -> item.getId().getProductId())
            .toList();
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

        if (!order.getUser().getId().equals(userId)) {
            log.warn("권한 없는 주문 조회 시도: userId={}, orderId={}", userId, orderId);
            throw new UnauthorizedOrderAccessException();
        }

        return OrderDetailResponse.from(order);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public CancelOrderResponse cancelOrder(Integer userId, Integer orderId, String cancelReason) {
        log.debug("주문 취소 시작: userId={}, orderId={}", userId, orderId);

        // 1. 주문 조회
        Order order = orderRepository.findByIdWithDetails(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2. 권한 확인
        if (!order.getUser().getId().equals(userId)) {
            log.warn("권한 없는 주문 취소 시도: userId={}, orderId={}", userId, orderId);
            throw new UnauthorizedOrderAccessException();
        }

        // 3. 취소 가능 여부 확인 및 취소
        if (!order.canCancel()) {
            throw new CannotCancelOrderException(order.getOrderStatus());
        }
        order.cancel(cancelReason);

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

        log.info("주문 취소 완료: orderId={}, userId={}, 환불액={}",
            orderId, userId, order.getFinalPrice());

        return CancelOrderResponse.from(order);
    }

    /**
     * 상품 조회 및 재고 확인하여 PriceItem 목록 생성
     */
    private List<PriceItem> createAndValidatePriceItems(List<CreateOrderRequest.OrderItemRequest> items) {
        List<Integer> productIds = items.stream()
            .map(CreateOrderRequest.OrderItemRequest::productId)
            .toList();

        Map<Integer, Product> productMap = new HashMap<>();
        productRepository.findAllById(productIds.stream()
                .map(Integer::longValue)
                .toList())
            .forEach(product -> productMap.put(product.getId().intValue(), product));

        List<PriceItem> priceItems = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest item : items) {
            Product product = productMap.get(item.productId());

            if (product == null) {
                throw new ProductNotFoundException(item.productId().longValue());
            }

            if (product.getStock() == null || product.getStock() < item.quantity()) {
                throw new InsufficientStockException(
                    product.getName(),
                    item.quantity(),
                    product.getStock() != null ? product.getStock() : 0
                );
            }

            priceItems.add(new PriceItem(product.getId(), product.getPrice(), item.quantity()));
        }

        return priceItems;
    }

    /**
     * 장바구니 기반 PriceItem 목록 생성 및 재고 확인
     */
    private List<PriceItem> createAndValidatePriceItemsFromCart(List<CartItem> cartItems) {
        List<Integer> productIds = cartItems.stream()
            .map(item -> item.getId().getProductId())
            .toList();

        Map<Integer, Product> productMap = new HashMap<>();
        productRepository.findAllById(productIds.stream()
                .map(Integer::longValue)
                .toList())
            .forEach(product -> productMap.put(product.getId().intValue(), product));

        List<PriceItem> priceItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Integer productId = cartItem.getId().getProductId();
            Product product = productMap.get(productId);

            if (product == null) {
                throw new ProductNotFoundException(productId.longValue());
            }

            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                    product.getName(),
                    cartItem.getQuantity(),
                    product.getStock() != null ? product.getStock() : 0
                );
            }

            priceItems.add(new PriceItem(product.getId(), product.getPrice(), cartItem.getQuantity()));
        }

        return priceItems;
    }

    /**
     * 쿠폰 검증 및 조회
     */
    private UserCoupon validateCoupon(Integer userId, Integer userCouponId, List<PriceItem> priceItems) {
        if (userCouponId == null) {
            return null;
        }

        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new CouponNotFoundException(userCouponId));

        if (!userCoupon.isUsable()) {
            throw new InvalidCouponException("사용할 수 없는 쿠폰입니다.");
        }

        if (!userCoupon.getUser().getId().equals(userId)) {
            throw new InvalidCouponException("본인의 쿠폰만 사용할 수 있습니다.");
        }

        Coupon coupon = userCoupon.getCoupon();
        BigDecimal totalProductPrice = priceItems.stream()
            .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (coupon.getMinOrderPrice() != null &&
            totalProductPrice.compareTo(coupon.getMinOrderPrice()) < 0) {
            throw new MinimumOrderNotMetException(totalProductPrice, coupon.getMinOrderPrice());
        }

        return userCoupon;
    }

    /**
     * 쿠폰 정보 추출
     */
    private CouponInfo extractCouponInfo(Coupon coupon) {
        return new CouponInfo(
            CouponType.valueOf(coupon.getCouponType().name()),
            coupon.getDiscountValue(),
            coupon.getMinOrderPrice()
        );
    }

    /**
     * OrderItem 생성 및 재고 차감
     */
    private void createOrderItemsAndDeductStock(Order order, List<CreateOrderRequest.OrderItemRequest> items) {
        List<Integer> productIds = items.stream()
            .map(CreateOrderRequest.OrderItemRequest::productId)
            .toList();

        Map<Integer, Product> productMap = new HashMap<>();
        productRepository.findAllById(productIds.stream()
                .map(Integer::longValue)
                .toList())
            .forEach(product -> productMap.put(product.getId().intValue(), product));

        for (CreateOrderRequest.OrderItemRequest item : items) {
            Product product = productMap.get(item.productId());

            OrderItem orderItem = new OrderItem(order, product, item.quantity(), product.getPrice());
            order.getOrderItems().add(orderItem);

            product.setStock(product.getStock() - item.quantity());
        }
    }

    /**
     * 장바구니 기반 OrderItem 생성 및 재고 차감
     */
    private void createOrderItemsFromCartAndDeductStock(Order order, List<CartItem> cartItems) {
        List<Integer> productIds = cartItems.stream()
            .map(item -> item.getId().getProductId())
            .toList();

        Map<Integer, Product> productMap = new HashMap<>();
        productRepository.findAllById(productIds.stream()
                .map(Integer::longValue)
                .toList())
            .forEach(product -> productMap.put(product.getId().intValue(), product));

        for (CartItem cartItem : cartItems) {
            Product product = productMap.get(cartItem.getId().getProductId());

            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity(), product.getPrice());
            order.getOrderItems().add(orderItem);

            product.setStock(product.getStock() - cartItem.getQuantity());
        }
    }
}