package kr.hhplus.be.server.application;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.UserCoupon;
import kr.hhplus.be.server.domain.order.*;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentMethod;
import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.point.PointType;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.presentation.dto.request.OrderItemRequest;
import kr.hhplus.be.server.presentation.dto.request.OrderRequest;
import kr.hhplus.be.server.presentation.dto.response.OrderResponse;
import kr.hhplus.be.server.service.PointHistoryService;
import kr.hhplus.be.server.service.cart.CartService;
import kr.hhplus.be.server.service.coupon.CouponService;
import kr.hhplus.be.server.service.order.OrderService;
import kr.hhplus.be.server.service.payment.PaymentService;
import kr.hhplus.be.server.service.product.ProductService;
import kr.hhplus.be.server.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
public class OrderPlaceFacade {

    private final CouponService couponService;
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final PointHistoryService pointHistoryService;
    private final PaymentService paymentService;
    private final CartService cartService;
    private final DataPlatformClient dataPlatformClient;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // 사전 유효성 체크
        User userInfo = userService.getUserInfo(request.userId());
        BigDecimal beforePoint = userInfo.getPoint();

        // 장바구니 체크
        List<Long> productIds = request.items().stream().map(OrderItemRequest::productId).toList();

        List<Cart> carts = cartService.completeCartItems(userInfo.getUserId(), productIds);
        if (carts.size() > 0) {
            if (carts.size() != productIds.size()) {
                throw new IllegalArgumentException("장바구니 정보가 일치하지 않거나 이미 처리되었습니다.");
            }
        }

        // 재고 점유 및 차감
        List<OrderItemRequest> orderItemRequests = request.items().stream().sorted(Comparator.comparing(OrderItemRequest::productId)).toList();
        Map<Long, String> productMap = new HashMap<>();
        for (OrderItemRequest item : orderItemRequests) {
            Product product = productService.decreaseStock(item.productId(), item.quantity());
            productMap.put(product.getProductId(), product.getProductName());
        }

        // 쿠폰  및 금액 계산
        BigDecimal totalOrderAmount = request.totalAmount();
        UserCoupon userCouponInfo = null;
        if (request.userCouponId() != null && request.userCouponId() > 0) {
            userCouponInfo = couponService.getUserCouponInfo(request.userCouponId(), request.userId());
            Coupon coupon = couponService.getUserCouponValidate(userCouponInfo.getCouponId());
            // 쿠폰할인 적용
            totalOrderAmount = coupon.applyDiscount(totalOrderAmount);

            BigDecimal addRequestItemPrice = orderItemRequests.stream().
                    map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal requestItemTotalPrice = coupon.applyDiscount(addRequestItemPrice);

            if (requestItemTotalPrice.compareTo(totalOrderAmount) != 0) {
                throw new IllegalArgumentException("주문 금액 정합성이 맞지 않습니다.");
            }

            // 쿠폰 사용
            userCouponInfo.use();
        }


        // 포인트 차감
        User user = userService.usePointWithOptimisticLock(userInfo.getUserId(), totalOrderAmount);
        PointHistory pointHistory = PointHistory.builder()
                .userId(userInfo.getUserId())
                .chargeAmount(totalOrderAmount)
                .paymentMethod(PaymentMethod.POINT)
                .beforePoint(beforePoint)
                .afterPoint(user.getPoint())
                .type(PointType.USE).build();

        // 포인트 내역 추가
        pointHistoryService.savePointHistory(pointHistory);

        // 주문 생성
        Order createOrder = Order.builder().userId(userInfo.getUserId())
                .totalPaymentAmount(totalOrderAmount)
                .orderStatus(OrderStatus.PAYMENT_COMPLETED)
                .userCouponId(userCouponInfo != null ? userCouponInfo.getUserCouponId() : null)
                .build();
        Order order = orderService.createOrder(createOrder);

        // 주문 생성 - 주문 아이템
        List<OrderItem> orderItems = createOrderItems(orderItemRequests, productMap, order);
        orderService.createOrderItem(orderItems);


        // 결제 생성
        Payment createPayment = Payment.builder()
                .orderId(order.getOrderId())
                .paymentAmount(totalOrderAmount)
                .paymentMethod(PaymentMethod.POINT)
                .build();
        createPayment.complete();
        paymentService.createPayment(createPayment);

        // 카트 상태 변경
        if (!carts.isEmpty()) {
            carts.forEach(Cart::markAsOrdered);
        }

        dataPlatformClient.sendOrderInfo(order);

        return OrderResponse.from(order);
//
//        if (true) {
//            throw new RuntimeException("DB에 저장되지 않아야 합니다!");
//        }
    }

    private static List<OrderItem> createOrderItems(List<OrderItemRequest> orderItemRequests, Map<Long, String> productMap, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest item : orderItemRequests) {
            String productName = productMap.get(item.productId());

            if (productName == null || productName.isEmpty()) {
                throw new IllegalStateException("상품 정보를 찾을 수 없습니다. ID: " + item.productId());
            }

            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("주문 수량은 1개 이상이어야 합니다.");
            }
            
            OrderItem createOrderItem = OrderItem.builder()
                    .orderId(order.getOrderId())
                    .productId(item.productId())
                    .productName(productMap.get(item.productId()))
                    .itemQuantity(item.quantity())
                    .paymentAmount(item.price().multiply(BigDecimal.valueOf(item.quantity())))
                    .build();
            orderItems.add(createOrderItem);
        }
        return orderItems;
    }
}
