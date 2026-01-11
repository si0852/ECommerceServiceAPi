package kr.hhplus.be.server.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    ORDER_COMPLETED("주문완료"),
    PAYMENT_COMPLETED("결제완료"),
    PAYMENT_FAILED("결제실패");

    private final String description;
}
