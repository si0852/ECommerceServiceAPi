package kr.hhplus.be.server.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CartStatus {
    ACTIVE("활성", "장바구니에 담겨 있는 상태"),
    ORDERED("주문완료", "주문이 완료되어 장바구니에서 빠진 상태"),
    DELETED("삭제", "사용자가 직접 삭제한 상태"),
    EXPIRED("만료", "보관 기간이 지나 시스템에 의해 만료된 상태");

    private final String text;
    private final String description;
}