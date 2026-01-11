package kr.hhplus.be.server.presentation.dto.response;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.user.User;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {

    private Long orderId;
    private BigDecimal totalPaymentAmount;
    private LocalDateTime paymentAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder().orderId(order.getOrderId()).totalPaymentAmount(order.getTotalPaymentAmount()).paymentAt(order.getPaymentAt()).build();
    }
}
