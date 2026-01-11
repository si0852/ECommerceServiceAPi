package kr.hhplus.be.server.presentation.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
        String userId,
        Long userCouponId,
        Long cartId,
        List<OrderItemRequest> items,
        BigDecimal totalAmount
) {
}
