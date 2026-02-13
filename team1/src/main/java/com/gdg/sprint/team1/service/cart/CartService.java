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
import com.gdg.sprint.team1.entity.Store;
import com.gdg.sprint.team1.exception.ProductNotFoundException;
import com.gdg.sprint.team1.repository.CartItemRepository;
import com.gdg.sprint.team1.repository.ProductRepository;
import com.gdg.sprint.team1.repository.StoreRepository;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       StoreRepository storeRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Integer userId) {
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
            .map(item -> item.getId().getProductId().longValue())
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Product> productMap = new HashMap<>();
        productRepository.findAllById(productIds)
            .forEach(product -> productMap.put(product.getId(), product));
        Map<Long, Store> storeMap = loadStores(productMap);

        List<CartItemResponse> items = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalProductPrice = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Integer productId = item.getId().getProductId();
            Product product = productMap.get(productId.longValue());
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
                productId,
                product != null ? product.getName() : null,
                productPrice,
                product != null ? product.getProductStatus() : null,
                product != null ? product.getStoreId() : null,
                toStoreName(product, storeMap),
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
    public void addItem(Integer userId, Integer productId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("quantity must be >= 1");
        }

        Product product = productRepository.findById(productId.longValue())
            .orElseThrow(() -> new ProductNotFoundException(productId.longValue()));

        Integer stock = product.getStock();
        if (stock != null && quantity > stock) {
            throw new IllegalArgumentException("OUT_OF_STOCK");
        }

        int updated = cartItemRepository.incrementQuantity(userId, productId, quantity);
        if (updated == 0) {
            try {
                cartItemRepository.save(new CartItem(userId, productId, quantity));
            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                int retried = cartItemRepository.incrementQuantity(userId, productId, quantity);
                if (retried == 0) {
                    throw new IllegalArgumentException("OUT_OF_STOCK");
                }
            }
        }
    }

    @Transactional
    public void updateQuantity(Integer userId, Integer productId, Integer quantity) {
        cartItemRepository.findByIdUserIdAndIdProductId(userId, productId)
            .ifPresent(item -> {
                if (quantity == null || quantity <= 0) {
                    cartItemRepository.deleteByIdUserIdAndIdProductId(userId, productId);
                } else {
                    Product product = productRepository.findById(productId.longValue())
                        .orElseThrow(() -> new ProductNotFoundException(productId.longValue()));
                    Integer stock = product.getStock();
                    if (stock != null && quantity > stock) {
                        throw new IllegalArgumentException("OUT_OF_STOCK");
                    }
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
    public void deleteItem(Integer userId, Integer productId) {
        cartItemRepository.deleteByIdUserIdAndIdProductId(userId, productId);
    }

    @Transactional
    public void deleteAll(Integer userId) {
        List<CartItem> items = cartItemRepository.findAllByIdUserId(userId);
        if (!items.isEmpty()) {
            cartItemRepository.deleteAll(items);
        }
    }

    public BigDecimal calculateDeliveryFee(BigDecimal totalProductPrice) {
        if (totalProductPrice == null || totalProductPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalProductPrice.compareTo(BigDecimal.valueOf(30000)) >= 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(3000);
    }

    private String toStoreName(Product product) {
        return toStoreName(product, Map.of());
    }

    private String toStoreName(Product product, Map<Long, Store> storeMap) {
        if (product == null) return null;
        if (product.getStore() != null) return product.getStore().getName();
        if (product.getStoreId() == null) return null;
        Store store = storeMap.get(product.getStoreId());
        return store != null ? store.getName() : null;
    }

    private Map<Long, Store> loadStores(Map<Long, Product> productMap) {
        List<Long> storeIds = productMap.values().stream()
            .map(Product::getStoreId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        if (storeIds.isEmpty()) return Map.of();
        return storeRepository.findAllById(storeIds).stream()
            .collect(Collectors.toMap(Store::getId, store -> store));
    }
}
