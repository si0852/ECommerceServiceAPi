package kr.hhplus.be.server.service.coupon;

import kr.hhplus.be.server.domain.coupon.DiscountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
class CouponServiceTest {

    @Test
    @DisplayName("성공: 주문금액 5000원일때 쿠폰 10% 할인시 500원 할인된다.")
    void percentage_discount() {
        BigDecimal originPrice = new BigDecimal("5000");
        BigDecimal percentage = new BigDecimal("10");

        BigDecimal percentagePrice = DiscountType.PERCENTAGE.calculateDiscount(originPrice, percentage);

        assertThat(percentagePrice).isEqualByComparingTo(new BigDecimal("500"));
    }
}