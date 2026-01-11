package kr.hhplus.be.server.presentation.dto.response;

import kr.hhplus.be.server.domain.user.User;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserResponse {

    private String userId;
    private BigDecimal chargePrice;

    public static UserResponse from(User user) {
        return UserResponse.builder().userId(user.getUserId()).chargePrice(user.getPoint()).build();
    }
}
