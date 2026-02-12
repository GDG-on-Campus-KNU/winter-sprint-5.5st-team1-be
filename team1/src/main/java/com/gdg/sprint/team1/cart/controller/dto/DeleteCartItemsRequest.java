package com.gdg.sprint.team1.cart.controller.dto;

import java.util.List;

public record DeleteCartItemsRequest (
    List<Integer> itemIds
) {}
