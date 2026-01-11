package kr.hhplus.be.server.domain.point;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.payment.PaymentMethod;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "charge_amount", precision = 10, scale = 2)
    private BigDecimal chargeAmount;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "charge_at")
    private LocalDateTime chargeAt;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private PointType type;

    @Column(name = "before_point")
    private BigDecimal beforePoint;

    @Column(name = "after_point")
    private BigDecimal afterPoint;

    @Builder
    public PointHistory(String userId, BigDecimal chargeAmount, PaymentMethod paymentMethod,
                        PointType type, BigDecimal beforePoint, BigDecimal afterPoint) {
        this.userId = userId;
        this.chargeAmount = chargeAmount;
        this.paymentMethod = paymentMethod;
        this.chargeAt = LocalDateTime.now();
        this.type = type;
        this.beforePoint = beforePoint;
        this.afterPoint = afterPoint;
    }
}
