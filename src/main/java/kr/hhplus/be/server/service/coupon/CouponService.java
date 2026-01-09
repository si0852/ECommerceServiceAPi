package kr.hhplus.be.server.service.coupon;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponStatus;
import kr.hhplus.be.server.domain.coupon.UserCoupon;
import kr.hhplus.be.server.repository.coupon.CouponRepository;
import kr.hhplus.be.server.repository.coupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public UserCoupon getUserCouponInfo(Long couponId, String userId) {
        return userCouponRepository.findByUserCouponIdAndUserIdAndCouponStatus(couponId, userId, CouponStatus.UNUSED)
                .orElseThrow(() -> new EntityNotFoundException("사용중인 쿠폰이 없음"));
    }

    public Coupon getUserCouponValidate(Long couponId) {
         return couponRepository.findValidCoupon(couponId, LocalDateTime.now())
                .orElseThrow(() -> new EntityNotFoundException("쿠폰 사용 기간이 아닙니다"));
    }

}
