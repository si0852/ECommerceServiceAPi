package kr.hhplus.be.server.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "total_payment_amount", precision = 12, scale = 2)
    private BigDecimal totalPaymentAmount;

    @Column(name = "order_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "user_coupon_id")
    private Long userCouponId;

    @Column(name = "payment_at")
    private LocalDateTime paymentAt;

    @Builder
    public Order(String userId, BigDecimal totalPaymentAmount, OrderStatus orderStatus, Long userCouponId) {
        this.userId = userId;
        this.totalPaymentAmount = totalPaymentAmount;
        this.orderStatus = orderStatus;
        this.userCouponId = userCouponId;
        this.paymentAt = LocalDateTime.now();
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

}
