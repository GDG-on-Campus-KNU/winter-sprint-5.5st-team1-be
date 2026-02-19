package com.gdg.sprint.team1.service;

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

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.domain.cart.CartItem;
import com.gdg.sprint.team1.dto.order.CancelOrderResponse;
import com.gdg.sprint.team1.dto.order.CouponContext;
import com.gdg.sprint.team1.dto.order.CreateOrderFromCartRequest;
import com.gdg.sprint.team1.dto.order.CreateOrderRequest;
import com.gdg.sprint.team1.dto.order.CreateOrderResponse;
import com.gdg.sprint.team1.dto.order.OrderDetailResponse;
import com.gdg.sprint.team1.dto.order.OrderResponse;
import com.gdg.sprint.team1.dto.pricing.PriceCalculationResult;
import com.gdg.sprint.team1.dto.pricing.PriceItem;
import com.gdg.sprint.team1.entity.Order;
import com.gdg.sprint.team1.entity.Order.OrderStatus;
import com.gdg.sprint.team1.entity.OrderItem;
import com.gdg.sprint.team1.entity.Product;
import com.gdg.sprint.team1.entity.User;
import com.gdg.sprint.team1.entity.UserCoupon;
import com.gdg.sprint.team1.exception.AuthRequiredException;
import com.gdg.sprint.team1.exception.CannotCancelOrderException;
import com.gdg.sprint.team1.exception.EmptyOrderException;
import com.gdg.sprint.team1.exception.InsufficientStockException;
import com.gdg.sprint.team1.exception.OrderNotFoundException;
import com.gdg.sprint.team1.exception.ProductNotFoundException;
import com.gdg.sprint.team1.exception.UnauthorizedOrderAccessException;
import com.gdg.sprint.team1.repository.OrderRepository;
import com.gdg.sprint.team1.repository.ProductRepository;
import com.gdg.sprint.team1.security.UserContextHolder;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PriceCalculationService priceCalculationService;
    private final UserService userService;
    private final CartService cartService;
    private final UserCouponService userCouponService;

    private Integer currentUserId() {
        Integer userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new AuthRequiredException();
        }
        return userId;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Integer userId = currentUserId();
        log.debug("주문 생성 시작: userId={}", userId);

        User user = userService.findById(userId);
        validateOrderItems(request.items());

        List<OrderItemInput> itemInputs = toOrderItemInputs(request.items());
        DeliveryInfo delivery = toDeliveryInfo(request);

        Order order = createOrderInternal(userId, user, itemInputs, delivery, request.userCouponId());

        log.info("주문 생성 완료: orderId={}, userId={}, finalPrice={}",
            order.getId(), userId, order.getFinalPrice());
        return CreateOrderResponse.from(order);
    }

    @Transactional
    public CreateOrderResponse createOrderFromCart(CreateOrderFromCartRequest request) {
        Integer userId = currentUserId();
        log.debug("장바구니 기반 주문 생성 시작: userId={}", userId);

        User user = userService.findById(userId);
        List<CartItem> cartItems = cartService.getCartItemsForOrder(userId);

        List<OrderItemInput> itemInputs = cartItems.stream()
            .map(c -> new OrderItemInput(c.getId().getProductId(), c.getQuantity()))
            .toList();
        DeliveryInfo delivery = toDeliveryInfo(request);

        Order order = createOrderInternal(userId, user, itemInputs, delivery, request.userCouponId());

        List<Long> productIds = cartItems.stream()
            .map(c -> c.getId().getProductId())
            .toList();
        cartService.clearCartItemsForOrder(userId, productIds);

        log.info("장바구니 기반 주문 생성 완료: orderId={}, userId={}, finalPrice={}",
            order.getId(), userId, order.getFinalPrice());
        return CreateOrderResponse.from(order);
    }

    private Order createOrderInternal(
        Integer userId,
        User user,
        List<OrderItemInput> itemInputs,
        DeliveryInfo delivery,
        Integer userCouponId
    ) {
        List<Long> productIds = itemInputs.stream().map(OrderItemInput::productId).toList();
        Map<Long, Product> productMap = loadProductMap(productIds);
        List<PriceItem> priceItems = buildPriceItemsAndValidateStock(itemInputs, productMap);
        CouponContext couponContext = userCouponService.resolveForOrder(userId, userCouponId, priceItems);

        PriceCalculationResult priceResult = priceCalculationService.calculateTotal(
            priceItems, couponContext.couponInfo());

        Order order = createAndSaveOrder(user, couponContext.userCoupon(), priceResult, delivery);
        addOrderItemsAndApplyStockAndCoupon(order, itemInputs, productMap, couponContext.userCoupon());

        return order;
    }

    private void validateOrderItems(List<CreateOrderRequest.OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new EmptyOrderException();
        }
    }

    private static List<OrderItemInput> toOrderItemInputs(List<CreateOrderRequest.OrderItemRequest> items) {
        return items.stream()
            .map(i -> new OrderItemInput(i.productId().longValue(), i.quantity()))
            .toList();
    }

    private static DeliveryInfo toDeliveryInfo(CreateOrderRequest request) {
        return new DeliveryInfo(
            request.recipientName(),
            request.recipientPhone(),
            request.deliveryAddress(),
            request.deliveryDetailAddress(),
            request.deliveryMessage()
        );
    }

    private static DeliveryInfo toDeliveryInfo(CreateOrderFromCartRequest request) {
        return new DeliveryInfo(
            request.recipientName(),
            request.recipientPhone(),
            request.deliveryAddress(),
            request.deliveryDetailAddress(),
            request.deliveryMessage()
        );
    }

    private Map<Long, Product> loadProductMap(List<Long> productIds) {
        Map<Long, Product> map = new HashMap<>();
        productRepository.findAllById(productIds)
            .forEach(p -> map.put(p.getId(), p));
        return map;
    }

    private List<PriceItem> buildPriceItemsAndValidateStock(
        List<OrderItemInput> itemInputs,
        Map<Long, Product> productMap
    ) {
        List<PriceItem> priceItems = new ArrayList<>();
        for (OrderItemInput input : itemInputs) {
            Product product = productMap.get(input.productId());
            if (product == null) {
                throw new ProductNotFoundException(input.productId());
            }
            if (product.getStock() == null || product.getStock() < input.quantity()) {
                throw new InsufficientStockException(
                    product.getName(),
                    input.quantity(),
                    product.getStock() != null ? product.getStock() : 0
                );
            }
            priceItems.add(new PriceItem(product.getId(), product.getPrice(), input.quantity()));
        }
        return priceItems;
    }

    private Order createAndSaveOrder(
        User user,
        UserCoupon userCoupon,
        PriceCalculationResult priceResult,
        DeliveryInfo delivery
    ) {
        Order order = Order.create(
            user,
            userCoupon,
            priceResult.totalProductPrice(),
            priceResult.discountAmount(),
            priceResult.deliveryFee(),
            priceResult.finalPrice(),
            delivery.recipientName(),
            delivery.recipientPhone(),
            delivery.deliveryAddress(),
            delivery.deliveryDetailAddress(),
            delivery.deliveryMessage()
        );
        orderRepository.save(order);
        return order;
    }

    private void addOrderItemsAndApplyStockAndCoupon(
        Order order,
        List<OrderItemInput> itemInputs,
        Map<Long, Product> productMap,
        UserCoupon userCoupon
    ) {
        for (OrderItemInput input : itemInputs) {
            Product product = productMap.get(input.productId());
            OrderItem orderItem = new OrderItem(order, product, input.quantity(), product.getPrice());
            order.addOrderItem(orderItem);
            product.deductStock(input.quantity());
        }
        if (userCoupon != null) {
            userCoupon.use();
        }
    }

    private record OrderItemInput(Long productId, Integer quantity) {}

    private record DeliveryInfo(
        String recipientName,
        String recipientPhone,
        String deliveryAddress,
        String deliveryDetailAddress,
        String deliveryMessage
    ) {}

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Integer page, Integer limit, String status) {
        Integer userId = currentUserId();
        int safePage = page != null && page >= 1 ? page : 1;
        int safeLimit = limit != null && limit >= 1 ? Math.min(limit, 100) : 10;

        log.debug("주문 목록 조회: userId={}, page={}, limit={}", userId, safePage, safeLimit);

        PageRequest pageable = PageRequest.of(
            safePage - 1,
            safeLimit,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Order> orderPage = findOrdersByUserAndStatus(userId, status, pageable);
        return orderPage.map(OrderResponse::from);
    }

    private Page<Order> findOrdersByUserAndStatus(Integer userId, String status, PageRequest pageable) {
        if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                return orderRepository.findAllByUserIdAndOrderStatus(userId, orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("유효하지 않은 주문 상태: {}, 전체 조회로 대체", status);
            }
        }
        return orderRepository.findAllByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Integer orderId) {
        Integer userId = currentUserId();
        log.debug("주문 상세 조회: userId={}, orderId={}", userId, orderId);

        Order order = orderRepository.findWithDetailsById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUser().getId().equals(userId)) {
            log.warn("권한 없는 주문 조회 시도: userId={}, orderId={}", userId, orderId);
            throw new UnauthorizedOrderAccessException();
        }

        return OrderDetailResponse.from(order);
    }

    @Transactional
    public CancelOrderResponse cancelOrder(Integer orderId, String cancelReason) {
        Integer userId = currentUserId();
        log.debug("주문 취소 시작: userId={}, orderId={}", userId, orderId);

        Order order = orderRepository.findWithDetailsById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUser().getId().equals(userId)) {
            log.warn("권한 없는 주문 취소 시도: userId={}, orderId={}", userId, orderId);
            throw new UnauthorizedOrderAccessException();
        }

        OrderStatus currentStatus = order.getOrderStatus();
        if (currentStatus != OrderStatus.PENDING && currentStatus != OrderStatus.CONFIRMED) {
            log.warn("취소 불가 상태: orderId={}, status={}", orderId, currentStatus);
            throw new CannotCancelOrderException(currentStatus);
        }

        restoreStockForOrderItems(order);
        restoreCouponIfUsed(order);
        order.cancel(cancelReason);

        log.info("주문 취소 완료: orderId={}, userId={}, 환불액={}",
            orderId, userId, order.getFinalPrice());

        return CancelOrderResponse.from(order);
    }

    private void restoreStockForOrderItems(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.restoreStock(orderItem.getQuantity());
            log.debug("재고 복구: productId={}, 복구량={}, 새 재고={}",
                product.getId(), orderItem.getQuantity(), product.getStock());
        }
    }

    private void restoreCouponIfUsed(Order order) {
        UserCoupon userCoupon = order.getUserCoupon();
        if (userCoupon != null) {
            userCoupon.restore();
            log.debug("쿠폰 복구: userCouponId={}", userCoupon.getId());
        }
    }
}
