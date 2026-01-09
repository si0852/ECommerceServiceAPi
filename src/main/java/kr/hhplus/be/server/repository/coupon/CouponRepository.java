package kr.hhplus.be.server.repository.coupon;

import kr.hhplus.be.server.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query(
            "SELECT c FROM Coupon c " +
            "WHERE c.couponId = :couponId " +
            "AND :now BETWEEN c.issuedAt AND c.expirationAt"
    )
    Optional<Coupon> findValidCoupon(@Param("couponId") Long couponId, @Param("now") LocalDateTime now);
}
