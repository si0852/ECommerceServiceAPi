package kr.hhplus.be.server.presentation.product;


import kr.hhplus.be.server.common.response.ApiResponse;
import kr.hhplus.be.server.presentation.dto.response.ProductResponse;
import kr.hhplus.be.server.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable("productId")Long productId) {
        ProductResponse response = productService.getProduct(productId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok(response));
    }
}
