package com.gdg.sprint.team1.dto.product;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchInfo(
    String keyword,
    long resultCount,
    Map<String, Object> filtersApplied
) {}
