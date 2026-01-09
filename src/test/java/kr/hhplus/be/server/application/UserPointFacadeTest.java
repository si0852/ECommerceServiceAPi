package kr.hhplus.be.server.application;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.service.PointHistoryService;
import kr.hhplus.be.server.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPointFacadeTest {

    @InjectMocks
    private UserPointFacade userPointFacade;

    @Mock
    private UserService userService;

    @Mock
    private PointHistoryService pointHistoryService;

    @Test
    @DisplayName("충전시 실패: 충전금액이 0이햐일때")
    void charge_Fail_Unit_Test() {
        String userId = "userId";
        Long chargePoint = 0L;

        assertThrows(IllegalArgumentException.class, () -> userPointFacade.chargePoint(userId, chargePoint));
    }

    @Test
    @DisplayName("충전시 실패: User가 존재하지 않을때")
    void charge_Fail_No_User_Test() {
        String userId = "userId";
        Long chargePoint = 10000L;

        when(userService.getUserInfoWithLock(userId)).thenThrow(new EntityNotFoundException("유저 없음"));

        assertThrows(EntityNotFoundException.class, () -> userPointFacade.chargePoint(userId, chargePoint));
    }

    @Test
    @DisplayName("충전 성공: chargePoint가 실행됬는지 확")
    void charge_Success_Charge_Point_Test() {
        String userId = "userId";
        Long chargePoint = 10000L;

        User mockUser = spy(User.builder().userId(userId).password("password").userName("sihyun").point(new BigDecimal(chargePoint)).build());

        when(userService.getUserInfoWithLock(userId)).thenReturn(mockUser);

        userPointFacade.chargePoint(userId, chargePoint);

        verify(mockUser, times(1)).chargePoint(new BigDecimal(chargePoint));

        ArgumentCaptor<PointHistory> pointHistoryArgumentCaptor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryService).savePointHistory(pointHistoryArgumentCaptor.capture());
    }

}