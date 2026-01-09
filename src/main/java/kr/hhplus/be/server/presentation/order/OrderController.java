package kr.hhplus.be.server.presentation.order;

import kr.hhplus.be.server.application.OrderPlaceFacade;
import kr.hhplus.be.server.common.response.ApiResponse;
import kr.hhplus.be.server.presentation.dto.request.OrderRequest;
import kr.hhplus.be.server.presentation.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderPlaceFacade orderPlaceFacade;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrderResponse>> OrderCreate(@RequestBody OrderRequest request) {
        OrderResponse order = orderPlaceFacade.createOrder(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(order));
    }
}
