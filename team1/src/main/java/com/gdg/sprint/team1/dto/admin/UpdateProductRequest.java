package com.gdg.sprint.team1.dto.admin;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 수정 요청")
public record UpdateProductRequest(

    @Schema(description = "상품명", example = "맥북 프로 16인치")
    @Size(max = 255)
    String name,

    @Schema(description = "상품 설명", example = "M3 Max 칩 탑재")
    @Size(max = 5000)
    String description,

    @Schema(description = "가격", example = "4000000")
    @Positive
    BigDecimal price,

    @Schema(description = "재고", example = "10")
    @Min(0)
    Integer stock
) {}