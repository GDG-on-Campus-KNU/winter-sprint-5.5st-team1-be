package com.gdg.sprint.team1.dto.cart;

import java.util.List;

public record DeleteCartItemsRequest (
    List<Integer> itemIds
) {}
