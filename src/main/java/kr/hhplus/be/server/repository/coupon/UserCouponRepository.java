package kr.hhplus.be.server.repository.coupon;

import kr.hhplus.be.server.domain.coupon.CouponStatus;
import kr.hhplus.be.server.domain.coupon.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    Optional<UserCoupon> findByUserCouponIdAndUserIdAndCouponStatus(Long userCouponId, String userId, CouponStatus couponStatus);
}
