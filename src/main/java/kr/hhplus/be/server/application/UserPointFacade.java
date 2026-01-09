package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.payment.PaymentMethod;
import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.point.PointType;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.presentation.dto.request.UserChargePointRequest;
import kr.hhplus.be.server.presentation.dto.response.UserResponse;
import kr.hhplus.be.server.service.PointHistoryService;
import kr.hhplus.be.server.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class UserPointFacade {

    private final UserService userService;
    private final PointHistoryService pointHistoryService;

    @Transactional
    public UserResponse chargePoint(String userId, Long chargePoint) {
        if (chargePoint <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        BigDecimal point = BigDecimal.valueOf(chargePoint);

        User userInfo = userService.getUserInfoWithLock(userId);
        BigDecimal beforePoint = userInfo.getPoint();
        userInfo.chargePoint(point);

        pointHistoryService.savePointHistory(PointHistory.builder().userId(userId).chargeAmount(point)
                .paymentMethod(PaymentMethod.POINT).type(PointType.CHARGE).beforePoint(beforePoint).afterPoint(userInfo.getPoint()).build());

        return UserResponse.from(userInfo);
    }

    @Transactional(readOnly = true)
    public UserResponse getPoint(String userId) {
        User userInfo = userService.getUserInfo(userId);

        return UserResponse.from(userInfo);
    }
}
