package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.presentation.dto.request.OrderItemRequest;
import kr.hhplus.be.server.presentation.dto.request.OrderRequest;
import kr.hhplus.be.server.repository.product.ProductRepository;
import kr.hhplus.be.server.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class OrderPlaceFacadeTest {

    @Autowired
    private OrderPlaceFacade orderPlaceFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("동시에 100명이 주문을 시도했을때")
    void concurrentOrderTest() throws InterruptedException {

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 1; i < threadCount; i++) {
            String userId = String.format("user%03d", i);

            executorService.submit(() -> {
                try {
                    OrderRequest request = createTestOrderRequest(userId, 101L, 1L);
                    orderPlaceFacade.createOrder(request);
                } catch (Exception e) {
                    System.out.println(userId + " 주문 시도 중 예외: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Product product = productRepository.findById(101L).orElseThrow();
        System.out.println("======================================");
        System.out.println("최종 남은 재고: " + product.getStockQuantity());
        System.out.println("======================================");
    }

    private OrderRequest createTestOrderRequest(String userId, Long productId, Long quantity) {

        OrderItemRequest orderItemRequest = new OrderItemRequest(productId, quantity, new BigDecimal("4500"));

        List<OrderItemRequest> items = List.of(orderItemRequest);

        return new OrderRequest(userId, null, null, items, new BigDecimal("4500.00"));
    }

}