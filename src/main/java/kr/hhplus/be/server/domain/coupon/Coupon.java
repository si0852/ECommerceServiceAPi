package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "coupon_name")
    private String couponName;

    @Column(name = "discount_type")
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "issued_quantity")
    private Long issuedQuantity;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "expiration_at")
    private LocalDateTime expirationAt;

    @Builder
    public Coupon(String couponName, DiscountType discountType, BigDecimal discountAmount,
                  Long totalQuantity) {
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0L;
        this.issuedAt = LocalDateTime.now();
        this.expirationAt = LocalDateTime.now().plusMonths(1);
    }

    public void issue() {
        if (this.issuedQuantity >= this.totalQuantity) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }
        this.issuedQuantity++;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expirationAt);
    }

    public boolean isAvailable() {
        return !isExpired() && this.issuedQuantity < this.totalQuantity;
    }

    public BigDecimal applyDiscount(BigDecimal totalAmount) {
        BigDecimal discountAmount = this.discountType.calculateDiscount(totalAmount, this.discountAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        return finalAmount.compareTo(BigDecimal.ZERO)<0 ? BigDecimal.ZERO : finalAmount;
    }
}
