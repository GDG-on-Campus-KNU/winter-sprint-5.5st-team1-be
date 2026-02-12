package com.gdg.sprint.team1.dto.product;

import java.util.List;

public record ProductListResponse(
    List<ProductListDto> products,
    SearchInfo searchInfo,
    PaginationInfo pagination
) {}
