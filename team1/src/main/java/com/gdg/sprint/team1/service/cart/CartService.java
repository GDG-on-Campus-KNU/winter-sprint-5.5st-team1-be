package com.gdg.sprint.team1.service.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdg.sprint.team1.domain.cart.CartItem;
import com.gdg.sprint.team1.dto.cart.CartItemResponse;
import com.gdg.sprint.team1.dto.cart.CartResponse;
import com.gdg.sprint.team1.dto.cart.CartSummary;
import com.gdg.sprint.team1.entity.Product;
import com.gdg.sprint.team1.exception.ProductNotFoundException;
import com.gdg.sprint.team1.repository.CartItemRepository;
import com.gdg.sprint.team1.repository.ProductRepository;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Integer userId) {
        List<CartItem> cartItems = cartItemRepository.findAllByIdUserId(userId);
        if (cartItems.isEmpty()) {
            CartSummary summary = new CartSummary(0, 0, 0, 0, 0);
            return new CartResponse(userId, List.of(), summary);
        }

        List<Long> productIds = cartItems.stream()
            .map(item -> item.getId().getProductId().longValue())
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Product> productMap = new HashMap<>();
        productRepository.findAllById(productIds)
            .forEach(product -> productMap.put(product.getId(), product));

        List<CartItemResponse> items = new ArrayList<>();
        int totalQuantity = 0;
        int totalProductPrice = 0;

        for (CartItem item : cartItems) {
            Integer productId = item.getId().getProductId();
            Product product = productMap.get(productId.longValue());
            Integer unitPrice = product != null ? toIntPrice(product.getPrice()) : 0;
            Integer quantity = item.getQuantity();
            Integer subtotal = unitPrice * quantity;
            boolean isAvailable = product != null
                && "ACTIVE".equals(product.getProductStatus())
                && product.getStock() != null
                && product.getStock() > 0;

            items.add(new CartItemResponse(
                productId,
                quantity,
                unitPrice,
                subtotal,
                isAvailable,
                item.getCreatedAt(),
                item.getUpdatedAt()
            ));

            totalQuantity += quantity;
            totalProductPrice += subtotal;
        }

        int deliveryFee = calculateDeliveryFee(totalProductPrice);
        CartSummary summary = new CartSummary(
            items.size(),
            totalQuantity,
            totalProductPrice,
            deliveryFee,
            totalProductPrice + deliveryFee
        );

        return new CartResponse(userId, items, summary);
    }

    @Transactional
    public void addItem(Integer userId, Integer productId, Integer quantity) {
        productRepository.findById(productId.longValue())
            .orElseThrow(() -> new ProductNotFoundException(productId.longValue()));

        cartItemRepository.findByIdUserIdAndIdProductId(userId, productId)
            .ifPresentOrElse(
                item -> item.setQuantity(item.getQuantity() + quantity),
                () -> cartItemRepository.save(new CartItem(userId, productId, quantity))
            );
    }

    @Transactional
    public void updateQuantity(Integer userId, Integer productId, Integer quantity) {
        cartItemRepository.findByIdUserIdAndIdProductId(userId, productId)
            .ifPresent(item -> {
                if (quantity <= 0) {
                    cartItemRepository.deleteByIdUserIdAndIdProductId(userId, productId);
                } else {
                    item.setQuantity(quantity);
                }
            });
    }

    @Transactional
    public void deleteSelected(Integer userId, List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return;
        cartItemRepository.deleteByUserIdAndProductIds(userId, productIds);
    }

    @Transactional
    public void deleteAll(Integer userId) {
        List<CartItem> items = cartItemRepository.findAllByIdUserId(userId);
        if (!items.isEmpty()) {
            cartItemRepository.deleteAll(items);
        }
    }

    public int calculateDeliveryFee(int totalProductPrice) {
        if (totalProductPrice == 0) return 0;
        return totalProductPrice >= 30000 ? 0 : 3000;
    }

    private int toIntPrice(BigDecimal price) {
        return price == null ? 0 : price.intValue();
    }
}
