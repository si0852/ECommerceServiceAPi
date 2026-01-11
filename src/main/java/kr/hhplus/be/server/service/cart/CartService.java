package kr.hhplus.be.server.service.cart;

import kr.hhplus.be.server.domain.order.Cart;
import kr.hhplus.be.server.domain.order.CartStatus;
import kr.hhplus.be.server.repository.order.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    @Transactional
    public List<Cart> completeCartItems(String userId, List<Long> productIds) {
        List<Cart> cartList = cartRepository.findByUserIdAndProductIdInAndStatus(userId, productIds, CartStatus.ACTIVE);

        return cartList;
    }
}
