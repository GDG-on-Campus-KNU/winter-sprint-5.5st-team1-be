package com.gdg.sprint.team1.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartItemRequest(
    @NotNull
    @Positive(message = "상품 ID는 양수여야 합니다.")
    Long productId,
    @NotNull
    @Min(1)
    Integer quantity
) {}
