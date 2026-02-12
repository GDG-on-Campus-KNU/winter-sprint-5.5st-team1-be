package com.gdg.sprint.team1.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaginationInfo(
    int currentPage,
    int totalPages,
    long totalItems,
    int itemsPerPage,
    boolean hasNext,
    boolean hasPrev
) {}
