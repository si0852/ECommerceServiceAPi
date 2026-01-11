package kr.hhplus.be.server.presentation.dto.request;

import java.math.BigDecimal;

public record OrderItemRequest(
        Long productId,
        Long quantity,
        BigDecimal price
) {
}
