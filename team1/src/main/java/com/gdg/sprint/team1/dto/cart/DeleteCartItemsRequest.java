package com.gdg.sprint.team1.dto.cart;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record DeleteCartItemsRequest (
    @NotNull List<Integer> itemIds
) {}
