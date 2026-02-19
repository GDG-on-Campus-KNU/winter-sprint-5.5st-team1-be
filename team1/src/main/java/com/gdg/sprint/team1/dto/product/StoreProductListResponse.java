package com.gdg.sprint.team1.dto.product;

import java.util.List;

public record StoreProductListResponse(
    StoreSummaryDto store,
    List<ProductListDto> products,
    PaginationInfo pagination
) {}
