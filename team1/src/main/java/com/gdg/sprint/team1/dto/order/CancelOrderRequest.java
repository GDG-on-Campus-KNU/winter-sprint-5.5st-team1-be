package com.gdg.sprint.team1.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "주문 취소 요청")
public record CancelOrderRequest(

    @Schema(
        description = "취소 사유 (선택, 최대 500자)",
        example = "단순 변심",
        nullable = true,
        maxLength = 500
    )
    @Size(max = 500, message = "취소 사유는 최대 500자까지 입력 가능합니다.")
    String cancelReason
) {}