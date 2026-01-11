package kr.hhplus.be.server.application;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponStatus;
import kr.hhplus.be.server.domain.coupon.DiscountType;
import kr.hhplus.be.server.domain.coupon.UserCoupon;
import kr.hhplus.be.server.domain.order.Cart;
import kr.hhplus.be.server.domain.order.CartStatus;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentMethod;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.presentation.dto.request.OrderItemRequest;
import kr.hhplus.be.server.presentation.dto.request.OrderRequest;
import kr.hhplus.be.server.service.PointHistoryService;
import kr.hhplus.be.server.service.cart.CartService;
import kr.hhplus.be.server.service.coupon.CouponService;
import kr.hhplus.be.server.service.order.OrderService;
import kr.hhplus.be.server.service.payment.PaymentService;
import kr.hhplus.be.server.service.product.ProductService;
import kr.hhplus.be.server.service.user.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPlaceFacadeMockTest {

    @InjectMocks
    private OrderPlaceFacade orderPlaceFacade;

    @Mock private CouponService couponService;
    @Mock private ProductService productService;
    @Mock private OrderService orderService;
    @Mock private UserService userService;
    @Mock private PointHistoryService pointHistoryService;
    @Mock private PaymentService paymentService;
    @Mock private CartService cartService;

    @Test
    @DisplayName("실패: 주문시 유저가 존재하지 않는 경우")
    void createOrder_Fail_Unit_Test() {
        String userId = "unknown_user";

        when(userService.getUserInfo(userId)).thenThrow(new EntityNotFoundException("유저 없음"));

        OrderItemRequest itemRequest = new OrderItemRequest(100L, 2L, new BigDecimal("25000"));
        OrderRequest request = new OrderRequest(userId, null, null, List.of(itemRequest), new BigDecimal("50000"));

        Assertions.assertThrows(EntityNotFoundException.class, () ->
                orderPlaceFacade.createOrder(request));
    }

    @Test
    @DisplayName("실패: 주문 요청한 상품 중 일부가 장바구니에 존재하지 않으면 예외가 발생한다")
    void createOrder_Fail_Cart_Unit_Test() {
        String userId = "sihyun";
        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(new BigDecimal("100000")).build();

        List<Long> requestProductIds = List.of(101L, 102L);
        List<Cart> carts = List.of(Cart.builder().userId(userId).productId(101L).quantity(2L).status(CartStatus.ACTIVE).build());

        when(userService.getUserInfo(userId)).thenReturn(mockUser);
        when(cartService.completeCartItems(userId, requestProductIds)).thenReturn(carts);


        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(101L, 1L, BigDecimal.TEN),
                new OrderItemRequest(102L, 1L, BigDecimal.TEN)
        );
        OrderRequest request = new OrderRequest(userId, 1L, 1L, items, new BigDecimal("20"));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                orderPlaceFacade.createOrder(request));

        verify(productService, never()).decreaseStock(anyLong(), anyLong());
        verify(paymentService, never()).createPayment(any());
    }

    @Test
    @DisplayName("실패: 주문한 모든 상품의 재고가 요청한 수보다 작다면 에러발생")
    void createOrder_Decrease_Stock_Fail_Test() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(new BigDecimal("100000")).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenThrow(new RuntimeException("재고가 부족합니다."));


        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("1000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 1L, new BigDecimal("2000"));
        OrderRequest request = new OrderRequest(userId, null, null, List.of(item1, item2), new BigDecimal("4000"));

        assertThrows(RuntimeException.class, () -> orderPlaceFacade.createOrder(request));
    }

    @Test
    @DisplayName("성공: 주문한 모든 상품의 재고가 상품별로 정확히 차감되어야 한다.")
    void createOrder_Decrease_Stock_Success_Test() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(new BigDecimal("100000")).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        when(userService.usePointWithOptimisticLock(anyString(), any())).thenReturn(User.builder().userId(userId).point(BigDecimal.ZERO).build());
        when(orderService.createOrder(any())).thenReturn(Order.builder().userId(userId).totalPaymentAmount(new BigDecimal("3000")).orderStatus(OrderStatus.PAYMENT_COMPLETED).userCouponId(null).build());

        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("1000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 1L, new BigDecimal("2000"));
        OrderRequest request = new OrderRequest(userId, null, null, List.of(item1, item2), new BigDecimal("4000"));

        orderPlaceFacade.createOrder(request);

        verify(productService, times(1)).decreaseStock(productAId, 2L);
        verify(productService, times(1)).decreaseStock(productBId, 1L);
    }

    @Test
    @DisplayName("실패: 유저가 보유하고 있는 쿠폰이 없을경우 ")
    void createOrder_Coupon_Fail_Test() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;
        Long couponId = 1L;

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(new BigDecimal("100000")).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        when(couponService.getUserCouponInfo(couponId, userId)).thenThrow(new EntityNotFoundException("사용중인 쿠폰이 없음"));
        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("1000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 1L, new BigDecimal("2000"));
        OrderRequest request = new OrderRequest(userId, couponId, null, List.of(item1, item2), new BigDecimal("4000"));

        assertThrows(EntityNotFoundException.class, () -> orderPlaceFacade.createOrder(request));

        verify(userService, never()).usePointWithOptimisticLock(anyString(), any());
        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("실패: 유저가 보유하고 있는 쿠폰이 만료될 경우 ")
    void createOrder_Coupon_Fail_Test2() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;
        Long couponId = 1L;

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(new BigDecimal("100000")).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        UserCoupon mockCoupon = UserCoupon.builder().userId(userId).couponId(couponId).couponStatus(CouponStatus.UNUSED).build();
        when(couponService.getUserCouponInfo(couponId, userId)).thenReturn(mockCoupon);
        when(couponService.getUserCouponValidate(couponId)).thenThrow(new EntityNotFoundException("쿠폰 사용 기간이 아닙니다"));

        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("1000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 1L, new BigDecimal("2000"));
        OrderRequest request = new OrderRequest(userId, couponId, null, List.of(item1, item2), new BigDecimal("4000"));

        assertThrows(EntityNotFoundException.class, () -> orderPlaceFacade.createOrder(request));

        verify(userService, never()).usePointWithOptimisticLock(anyString(), any());
        verify(orderService, never()).createOrder(any());
        verify(paymentService, never()).createPayment(any());
    }

    @Test
    @DisplayName("성공: 쿠폰할인 적용된 가격, 쿠폰 사용 상태 변경")
    void createOrder_Coupon_Success_Test() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;
        Long couponId = 1L;

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(new BigDecimal("100000")).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        UserCoupon mockCoupon = UserCoupon.builder().userId(userId).couponId(couponId).couponStatus(CouponStatus.UNUSED).build();
        UserCoupon spyCoupon = spy(mockCoupon);
        when(couponService.getUserCouponInfo(couponId, userId)).thenReturn(spyCoupon);

        Coupon couponA = Coupon.builder().couponName("couponA").discountType(DiscountType.PERCENTAGE).discountAmount(new BigDecimal("10")).totalQuantity(3L).build();
        when(couponService.getUserCouponValidate(couponId)).thenReturn(couponA);

        User updatedUser = User.builder().userId(userId).password("password").userName("sihyun").point(new BigDecimal("96400")).build();
        when(userService.usePointWithOptimisticLock(eq(userId), any(BigDecimal.class))).thenReturn(updatedUser);

        Order savedOrder = Order.builder().userId(userId).totalPaymentAmount(new BigDecimal("1")).orderStatus(OrderStatus.ORDER_COMPLETED).userCouponId(mockCoupon.getUserCouponId()).build();
        when(orderService.createOrder(any(Order.class))).thenReturn(savedOrder);

        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("1000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 1L, new BigDecimal("2000"));
        BigDecimal totalPrice = new BigDecimal("4000");
        OrderRequest request = new OrderRequest(userId, couponId, null, List.of(item1, item2), totalPrice);

        orderPlaceFacade.createOrder(request);

        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(userService).usePointWithOptimisticLock(eq(userId), amountCaptor.capture());

        assertThat(amountCaptor.getValue()).isEqualByComparingTo(new BigDecimal("3600"));

        verify(spyCoupon, times(1)).use();
    }

    @Test
    @DisplayName("실패: 쿠폰적용된 합계금액이 유저가 소유한 포인트보다 크다면 에러 발생")
    void createOrder_Point_Fail_Test() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;
        Long couponId = 1L;

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(new BigDecimal("100000")).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        UserCoupon mockCoupon = UserCoupon.builder().userId(userId).couponId(couponId).couponStatus(CouponStatus.UNUSED).build();
        when(couponService.getUserCouponInfo(couponId, userId)).thenReturn(mockCoupon);

        Coupon couponA = Coupon.builder().couponName("couponA").discountType(DiscountType.PERCENTAGE).discountAmount(new BigDecimal("10")).totalQuantity(3L).build();
        when(couponService.getUserCouponValidate(couponId)).thenReturn(couponA);

        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("10000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 5L, new BigDecimal("20000"));
        BigDecimal totalPrice = new BigDecimal("120000");
        OrderRequest request = new OrderRequest(userId, couponId, null, List.of(item1, item2), totalPrice);

        when(userService.usePointWithOptimisticLock(eq(userId), any(BigDecimal.class)))
                .thenThrow(new IllegalStateException("잔액이 부족합니다."));

        assertThrows(IllegalStateException.class,() -> orderPlaceFacade.createOrder(request));

        verify(orderService, never()).createOrder(any());
        verify(paymentService, never()).createPayment(any());
    }

    @Test
    @DisplayName("실패: 쿠폰적용된 합계금액과 요청 상품에 대한 합계 금액이 맞지 않을 경우")
    void createOrder_Point_Fail_Test2() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;
        Long couponId = 1L;

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(new BigDecimal("100000")).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        UserCoupon mockCoupon = UserCoupon.builder().userId(userId).couponId(couponId).couponStatus(CouponStatus.UNUSED).build();
        when(couponService.getUserCouponInfo(couponId, userId)).thenReturn(mockCoupon);

        Coupon couponA = Coupon.builder().couponName("couponA").discountType(DiscountType.PERCENTAGE).discountAmount(new BigDecimal("10")).totalQuantity(3L).build();
        when(couponService.getUserCouponValidate(couponId)).thenReturn(couponA);

        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("10001"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 5L, new BigDecimal("20000"));
        BigDecimal totalPrice = new BigDecimal("120000");
        OrderRequest request = new OrderRequest(userId, couponId, null, List.of(item1, item2), totalPrice);

        assertThrows(IllegalArgumentException.class,() -> orderPlaceFacade.createOrder(request));

        verify(orderService, never()).createOrder(any());
        verify(paymentService, never()).createPayment(any());
    }

    @Test
    @DisplayName("성공: 포인트 차감")
    void createOrder_Point_Success_Test() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;
        Long couponId = 1L;
        BigDecimal initialAmount = new BigDecimal("100000");

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(initialAmount).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        UserCoupon mockCoupon = UserCoupon.builder().userId(userId).couponId(couponId).couponStatus(CouponStatus.UNUSED).build();
        UserCoupon spyCoupon = spy(mockCoupon);
        when(couponService.getUserCouponInfo(couponId, userId)).thenReturn(spyCoupon);

        Coupon couponA = Coupon.builder().couponName("couponA").discountType(DiscountType.PERCENTAGE).discountAmount(new BigDecimal("10")).totalQuantity(3L).build();
        when(couponService.getUserCouponValidate(couponId)).thenReturn(couponA);

        BigDecimal expectedAmount = new BigDecimal("96400");
        User updatedUser = User.builder().userId(userId).password("password").userName("sihyun").point(expectedAmount).build();
        when(userService.usePointWithOptimisticLock(eq(userId), any(BigDecimal.class))).thenReturn(updatedUser);

        Order savedOrder = Order.builder().userId(userId).totalPaymentAmount(new BigDecimal("1")).orderStatus(OrderStatus.ORDER_COMPLETED).userCouponId(mockCoupon.getUserCouponId()).build();
        when(orderService.createOrder(any(Order.class))).thenReturn(savedOrder);

        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("1000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 1L, new BigDecimal("2000"));
        BigDecimal totalPrice = new BigDecimal("4000");
        OrderRequest request = new OrderRequest(userId, couponId, null, List.of(item1, item2), totalPrice);


        orderPlaceFacade.createOrder(request);

        ArgumentCaptor<PointHistory> historyCaptor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryService).savePointHistory(historyCaptor.capture());

        assertThat(historyCaptor.getValue().getAfterPoint()).isEqualByComparingTo(expectedAmount);
    }

    @Test
    @DisplayName("성공: 주문 생성시 상태값이 PAYMENT_COMPLETED인지 확인")
    void createOrder_OrderAndPayment_Success_Test() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;
        Long couponId = 1L;
        BigDecimal initialAmount = new BigDecimal("100000");

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(initialAmount).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        UserCoupon mockCoupon = UserCoupon.builder().userId(userId).couponId(couponId).couponStatus(CouponStatus.UNUSED).build();
        UserCoupon spyCoupon = spy(mockCoupon);
        when(couponService.getUserCouponInfo(couponId, userId)).thenReturn(spyCoupon);

        Coupon couponA = Coupon.builder().couponName("couponA").discountType(DiscountType.PERCENTAGE).discountAmount(new BigDecimal("10")).totalQuantity(3L).build();
        when(couponService.getUserCouponValidate(couponId)).thenReturn(couponA);

        BigDecimal expectedAmount = new BigDecimal("96400");
        User updatedUser = User.builder().userId(userId).password("password").userName("sihyun").point(expectedAmount).build();
        when(userService.usePointWithOptimisticLock(eq(userId), any(BigDecimal.class))).thenReturn(updatedUser);

        Order savedOrder = Order.builder().userId(userId).totalPaymentAmount(new BigDecimal("1")).orderStatus(OrderStatus.ORDER_COMPLETED).userCouponId(mockCoupon.getUserCouponId()).build();
        when(orderService.createOrder(any(Order.class))).thenReturn(savedOrder);

        BigDecimal totalPrice = new BigDecimal("4000");

        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("1000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 1L, new BigDecimal("2000"));

        OrderRequest request = new OrderRequest(userId, couponId, null, List.of(item1, item2), totalPrice);

        orderPlaceFacade.createOrder(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderService).createOrder(orderCaptor.capture());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentService).createPayment(paymentCaptor.capture());

        assertThat(orderCaptor.getValue().getOrderStatus()).isEqualByComparingTo(OrderStatus.PAYMENT_COMPLETED);
        assertThat(paymentCaptor.getValue().getPaymentStatus()).isEqualByComparingTo(PaymentStatus.COMPLETED);
        assertThat(paymentCaptor.getValue().getPaymentMethod()).isEqualByComparingTo(PaymentMethod.POINT);
    }

    @Test
    @DisplayName("실패: 주문아이템 생성시 상품정보 정보가 유효하지 않다면")
    void createOrder_OrderAndPayment_OrderItem_Fail_Test() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;
        Long couponId = 1L;
        BigDecimal initialAmount = new BigDecimal("100000");

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(initialAmount).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        UserCoupon mockCoupon = UserCoupon.builder().userId(userId).couponId(couponId).couponStatus(CouponStatus.UNUSED).build();
        UserCoupon spyCoupon = spy(mockCoupon);
        when(couponService.getUserCouponInfo(couponId, userId)).thenReturn(spyCoupon);

        Coupon couponA = Coupon.builder().couponName("couponA").discountType(DiscountType.PERCENTAGE).discountAmount(new BigDecimal("10")).totalQuantity(3L).build();
        when(couponService.getUserCouponValidate(couponId)).thenReturn(couponA);

        BigDecimal expectedAmount = new BigDecimal("96400");
        User updatedUser = User.builder().userId(userId).password("password").userName("sihyun").point(expectedAmount).build();
        when(userService.usePointWithOptimisticLock(eq(userId), any(BigDecimal.class))).thenReturn(updatedUser);

        Order savedOrder = Order.builder().userId(userId).totalPaymentAmount(new BigDecimal("1")).orderStatus(OrderStatus.ORDER_COMPLETED).userCouponId(mockCoupon.getUserCouponId()).build();
        when(orderService.createOrder(any(Order.class))).thenReturn(savedOrder);

        BigDecimal totalPrice = new BigDecimal("4000");

        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("1000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 1L, new BigDecimal("2000"));

        OrderRequest request = new OrderRequest(userId, couponId, null, List.of(item1, item2), totalPrice);

        assertThrows(IllegalStateException.class, () -> orderPlaceFacade.createOrder(request));
    }

    @Test
    @DisplayName("실패: 주문아이템 생성시 결제 요청 상품수량이 0 이하일 경우")
    void createOrder_OrderAndPayment_OrderItem_Fail_Test2() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;
        Long couponId = 1L;
        BigDecimal initialAmount = new BigDecimal("100000");

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(initialAmount).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        UserCoupon mockCoupon = UserCoupon.builder().userId(userId).couponId(couponId).couponStatus(CouponStatus.UNUSED).build();
        UserCoupon spyCoupon = spy(mockCoupon);
        when(couponService.getUserCouponInfo(couponId, userId)).thenReturn(spyCoupon);

        Coupon couponA = Coupon.builder().couponName("couponA").discountType(DiscountType.PERCENTAGE).discountAmount(new BigDecimal("10")).totalQuantity(3L).build();
        when(couponService.getUserCouponValidate(couponId)).thenReturn(couponA);

        BigDecimal expectedAmount = new BigDecimal("96400");
        User updatedUser = User.builder().userId(userId).password("password").userName("sihyun").point(expectedAmount).build();
        when(userService.usePointWithOptimisticLock(eq(userId), any(BigDecimal.class))).thenReturn(updatedUser);

        Order savedOrder = Order.builder().userId(userId).totalPaymentAmount(new BigDecimal("1")).orderStatus(OrderStatus.ORDER_COMPLETED).userCouponId(mockCoupon.getUserCouponId()).build();
        when(orderService.createOrder(any(Order.class))).thenReturn(savedOrder);

        BigDecimal totalPrice = new BigDecimal("2000");

        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("1000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 0L, new BigDecimal("2000"));

        OrderRequest request = new OrderRequest(userId, couponId, null, List.of(item1, item2), totalPrice);

        assertThrows(IllegalArgumentException.class, () -> orderPlaceFacade.createOrder(request));
    }

    @Test
    @DisplayName("성공: 카트 상태변경 됬는지 확인")
    void createOrder_OrderAndPayment_cart_status_Success_Test() {
        String userId = "sihyun";
        Long productAId = 100L;
        Long productBId = 200L;
        Long cartAId = 10L;
        Long couponId = 1L;
        BigDecimal initialAmount = new BigDecimal("100000");

        User mockUser = User.builder().userId(userId).password("password").userName("sihyun").point(initialAmount).build();
        when(userService.getUserInfo(userId)).thenReturn(mockUser);

        Cart cartA = spy(Cart.builder().cartId(cartAId).userId(userId).productId(productAId).quantity(2L).status(CartStatus.ACTIVE).build());
        Cart cartB = spy(Cart.builder().cartId(cartAId).userId(userId).productId(productBId).quantity(1L).status(CartStatus.ACTIVE).build());

        when(cartService.completeCartItems(userId, List.of(productAId, productBId))).thenReturn(List.of(cartA, cartB));

        Product productA = Product.builder().productId(productAId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();
        Product productB = Product.builder().productId(productBId).productName("상품A").stockQuantity(10L).productPrice(new BigDecimal("1500")).productDesc("A desc").status("status").build();

        when(productService.decreaseStock(eq(productAId), anyLong())).thenReturn(productA);
        when(productService.decreaseStock(eq(productBId), anyLong())).thenReturn(productB);

        UserCoupon mockCoupon = UserCoupon.builder().userId(userId).couponId(couponId).couponStatus(CouponStatus.UNUSED).build();
        UserCoupon spyCoupon = spy(mockCoupon);
        when(couponService.getUserCouponInfo(couponId, userId)).thenReturn(spyCoupon);

        Coupon couponA = Coupon.builder().couponName("couponA").discountType(DiscountType.PERCENTAGE).discountAmount(new BigDecimal("10")).totalQuantity(3L).build();
        when(couponService.getUserCouponValidate(couponId)).thenReturn(couponA);

        BigDecimal expectedAmount = new BigDecimal("96400");
        User updatedUser = User.builder().userId(userId).password("password").userName("sihyun").point(expectedAmount).build();
        when(userService.usePointWithOptimisticLock(eq(userId), any(BigDecimal.class))).thenReturn(updatedUser);

        Order savedOrder = Order.builder().userId(userId).totalPaymentAmount(new BigDecimal("1")).orderStatus(OrderStatus.ORDER_COMPLETED).userCouponId(mockCoupon.getUserCouponId()).build();
        when(orderService.createOrder(any(Order.class))).thenReturn(savedOrder);

        BigDecimal totalPrice = new BigDecimal("4000");

        OrderItemRequest item1 = new OrderItemRequest(productAId, 2L, new BigDecimal("1000"));
        OrderItemRequest item2 = new OrderItemRequest(productBId, 1L, new BigDecimal("2000"));

        OrderRequest request = new OrderRequest(userId, couponId, cartAId, List.of(item1, item2), totalPrice);

        orderPlaceFacade.createOrder(request);

        verify(cartA, times(1)).markAsOrdered();
        verify(cartB, times(1)).markAsOrdered();
        assertThat(cartA.getStatus()).isEqualTo(CartStatus.ORDERED);
        assertThat(cartB.getStatus()).isEqualTo(CartStatus.ORDERED);
    }

}