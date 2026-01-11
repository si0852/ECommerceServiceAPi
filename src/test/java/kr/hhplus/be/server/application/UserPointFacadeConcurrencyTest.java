package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.presentation.dto.request.OrderItemRequest;
import kr.hhplus.be.server.presentation.dto.request.OrderRequest;
import kr.hhplus.be.server.repository.product.ProductRepository;
import kr.hhplus.be.server.repository.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class UserPointFacadeConcurrencyTest {

    @Autowired
    private UserPointFacade userPointFacade;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("같은 유저가 따딱 클릭했을때 2번 연속 충전을 진행할때")
    void concurrentChargePointTest() throws InterruptedException {

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Long chargePoint = 10000L;
        String userId = "user010";
        User beforeUser = userRepository.findByUserId(userId).get();
        System.out.println("Before User Point : " + beforeUser.getPoint());

        for (int i = 0; i < threadCount; i++) {

            executorService.submit(() -> {
                try{
                    userPointFacade.chargePoint(userId, chargePoint);
                }catch (Exception e) {
                    System.out.println("중복 클릭 방지 성공: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        User afterUser = userRepository.findByUserId(userId).get();
        System.out.println("After User Point : " + afterUser.getPoint());

        assertThat(beforeUser.getPoint().add(new BigDecimal(chargePoint))).isEqualByComparingTo(afterUser.getPoint());
    }



}