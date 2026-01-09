package kr.hhplus.be.server.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "item_quantity")
    private Long itemQuantity;

    @Column(name = "payment_amount", precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_at")
    private LocalDateTime paymentAt;

    @Builder
    public OrderItem(Long orderId, Long productId, String productName,
                     Long itemQuantity, BigDecimal paymentAmount) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.itemQuantity = itemQuantity;
        this.paymentAmount = paymentAmount;
        this.paymentAt = LocalDateTime.now();
    }
}
