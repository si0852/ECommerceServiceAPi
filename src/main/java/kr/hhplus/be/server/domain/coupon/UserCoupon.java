package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    private Long userCouponId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "coupon_status")
    @Enumerated(EnumType.STRING)
    private CouponStatus couponStatus;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Builder
    public UserCoupon(String userId, Long couponId, CouponStatus couponStatus) {
        this.userId = userId;
        this.couponId = couponId;
        this.couponStatus = couponStatus;
    }

    public void use() {
        if (CouponStatus.USED.equals(this.couponStatus)) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        this.couponStatus = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public boolean isUsable() {
        return "AVAILABLE".equals(this.couponStatus);
    }
}
