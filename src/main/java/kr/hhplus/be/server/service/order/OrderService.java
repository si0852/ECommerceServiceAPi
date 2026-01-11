package kr.hhplus.be.server.service.order;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.domain.order.Cart;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderItem;
import kr.hhplus.be.server.repository.order.CartRepository;
import kr.hhplus.be.server.repository.order.OrderItemRepository;
import kr.hhplus.be.server.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException());
    }

    @Transactional
    public Order createOrder(Order order){
        return orderRepository.save(order);
    }


    @Transactional
    public void createOrderItem(List<OrderItem> orderItem){
         orderItemRepository.saveAll(orderItem);
    }

    public List<Cart> getCart(String userId) {
        return cartRepository.findByUserId(userId);
    }

}
