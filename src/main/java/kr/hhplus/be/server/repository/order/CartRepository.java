package kr.hhplus.be.server.repository.order;

import kr.hhplus.be.server.domain.order.Cart;
import kr.hhplus.be.server.domain.order.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUserId(String userId);
    List<Cart> findByUserIdAndProductIdInAndStatus(String userId, List<Long> productIds, CartStatus status);
}
