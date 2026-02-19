package com.gdg.sprint.team1.service.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.gdg.sprint.team1.domain.cart.CartItem;
import com.gdg.sprint.team1.dto.cart.CartItemResponse;
import com.gdg.sprint.team1.dto.cart.CartResponse;
import com.gdg.sprint.team1.dto.cart.CartSummary;
import com.gdg.sprint.team1.entity.Product;
import com.gdg.sprint.team1.exception.AuthRequiredException;
import com.gdg.sprint.team1.exception.CartItemNotFoundException;
import com.gdg.sprint.team1.exception.EmptyCartException;
import com.gdg.sprint.team1.exception.InsufficientStockException;
import com.gdg.sprint.team1.exception.ProductNotFoundException;
import com.gdg.sprint.team1.repository.CartItemRepository;
import com.gdg.sprint.team1.repository.ProductRepository;
import com.gdg.sprint.team1.security.UserContextHolder;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("30000");
    private static final BigDecimal DEFAULT_DELIVERY_FEE = new BigDecimal("3000");

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    private Integer currentUserId() {
        Integer userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw new AuthRequiredException();
        }
        return userId;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        Integer userId = currentUserId();
        List<CartItem> cartItems = cartItemRepository.findAllByIdUserId(userId);
        if (cartItems.isEmpty()) {
            CartSummary summary = new CartSummary(
                0,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            );
            return new CartResponse(userId, List.of(), summary);
        }

        List<Long> productIds = cartItems.stream()
            .map(item -> item.getId().getProductId())
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Product> productMap = new HashMap<>();
        productRepository.findAllById(productIds)
            .forEach(product -> productMap.put(product.getId(), product));

        List<CartItemResponse> items = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalProductPrice = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Long productId = item.getId().getProductId();
            Product product = productMap.get(productId);
            BigDecimal productPrice = product != null && product.getPrice() != null
                ? product.getPrice()
                : BigDecimal.ZERO;
            Integer quantity = item.getQuantity();
            BigDecimal subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));
            boolean isAvailable = product != null
                && "ACTIVE".equals(product.getProductStatus())
                && product.getStock() != null
                && product.getStock() >= quantity;

            items.add(new CartItemResponse(
                productId != null ? productId.intValue() : null,
                product != null ? product.getName() : null,
                productPrice,
                product != null ? product.getProductStatus() : null,
                quantity,
                subtotal,
                isAvailable,
                item.getCreatedAt(),
                item.getUpdatedAt()
            ));

            totalQuantity += quantity;
            totalProductPrice = totalProductPrice.add(subtotal);
        }

        BigDecimal deliveryFee = calculateDeliveryFee(totalProductPrice);
        CartSummary summary = new CartSummary(
            items.size(),
            totalQuantity,
            totalProductPrice,
            deliveryFee,
            totalProductPrice.add(deliveryFee)
        );

        return new CartResponse(userId, items, summary);
    }

    @Transactional
    public void addItem(Integer productId, Integer quantity) {
        Integer userId = currentUserId();
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }

        Product product = productRepository.findById(productId.longValue())
            .orElseThrow(() -> new ProductNotFoundException(productId.longValue()));

        Integer stock = product.getStock();
        if (stock != null && quantity > stock) {
            throw new InsufficientStockException(
                product.getName(),
                quantity,
                stock
            );
        }

        Long productIdLong = productId.longValue();
        int updated = cartItemRepository.incrementQuantity(userId, productIdLong, quantity);
        if (updated == 0) {
            try {
                cartItemRepository.save(new CartItem(userId, productIdLong, quantity));
            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                int retried = cartItemRepository.incrementQuantity(userId, productIdLong, quantity);
                if (retried > 0) {
                    return;
                }
                throw ex;
            }
        }
    }

    @Transactional
    public void updateQuantity(Integer productId, Integer quantity) {
        Integer userId = currentUserId();
        Long productIdLong = productId.longValue();
        CartItem item = cartItemRepository.findByIdUserIdAndIdProductId(userId, productIdLong)
            .orElseThrow(() -> new CartItemNotFoundException(productId));
        if (quantity == null || quantity <= 0) {
            cartItemRepository.deleteByIdUserIdAndIdProductId(userId, productIdLong);
        } else {
            Product product = productRepository.findById(productIdLong)
                .orElseThrow(() -> new ProductNotFoundException(productIdLong));
            Integer stock = product.getStock();
            if (stock != null && quantity > stock) {
                throw new InsufficientStockException(
                    product.getName(),
                    quantity,
                    stock
                );
            }
            item.updateQuantity(quantity);
        }
    }

    @Transactional
    public void deleteSelected(List<Integer> productIds) {
        Integer userId = currentUserId();
        if (productIds == null || productIds.isEmpty()) return;
        List<Long> productIdLongs = productIds.stream().map(Integer::longValue).collect(Collectors.toList());
        cartItemRepository.deleteById_UserIdAndId_ProductIdIn(userId, productIdLongs);
    }

    @Transactional
    public void deleteItem(Integer productId) {
        Integer userId = currentUserId();
        cartItemRepository.deleteByIdUserIdAndIdProductId(userId, productId.longValue());
    }

    @Transactional
    public void deleteAll() {
        Integer userId = currentUserId();
        List<CartItem> items = cartItemRepository.findAllByIdUserId(userId);
        if (!items.isEmpty()) {
            cartItemRepository.deleteAll(items);
        }
    }

    /**
     * 주문용 장바구니 항목 조회. 비어 있으면 EmptyCartException.
     * OrderService에서 장바구니 기반 주문 시 사용한다.
     */
    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsForOrder(Integer userId) {
        List<CartItem> items = cartItemRepository.findAllByIdUserId(userId);
        if (items.isEmpty()) {
            throw new EmptyCartException();
        }
        return items;
    }

    /**
     * 주문 반영 후 해당 상품들을 장바구니에서 제거.
     * OrderService에서 장바구니 기반 주문 완료 후 호출한다.
     */
    @Transactional
    public void clearCartItemsForOrder(Integer userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        cartItemRepository.deleteById_UserIdAndId_ProductIdIn(userId, productIds);
    }

    public BigDecimal calculateDeliveryFee(BigDecimal totalProductPrice) {
        if (totalProductPrice == null || totalProductPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalProductPrice.compareTo(FREE_DELIVERY_THRESHOLD) >= 0
            ? BigDecimal.ZERO
            : DEFAULT_DELIVERY_FEE;
    }
}
