package kr.hhplus.be.server.domain.coupon;

import java.math.BigDecimal;

public enum DiscountType {

    FIXED_AMOUNT{
        @Override
        public BigDecimal calculateDiscount(BigDecimal originPrice, BigDecimal discountPrice) {
            return discountPrice;
        }
    },

    PERCENTAGE{
        @Override
        public BigDecimal calculateDiscount(BigDecimal originPrice, BigDecimal discountPrice) {
            return originPrice.multiply(discountPrice.divide(new BigDecimal("100")));
        }
    };

    public abstract BigDecimal calculateDiscount(BigDecimal originPrice, BigDecimal discountPrice);
}
