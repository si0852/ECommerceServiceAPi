package kr.hhplus.be.server.infrastructure.dataplatform;

import kr.hhplus.be.server.domain.order.DataPlatformClient;
import kr.hhplus.be.server.domain.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FakeDataPlatformClient implements DataPlatformClient {
    @Override
    public void sendOrderInfo(Order order) {
        log.info("[데이터 플랫폼] 주문 정보 전송: orderId = {}", order.getOrderId());
    }
}
