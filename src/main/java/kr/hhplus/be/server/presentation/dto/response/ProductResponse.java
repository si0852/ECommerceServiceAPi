package kr.hhplus.be.server.presentation.dto.response;

import kr.hhplus.be.server.domain.product.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {

    private Long productId;
    private String productName;
    private BigDecimal price;
    private Long quantity;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder().productId(product.getProductId()).productName(product.getProductName())
                .price(product.getProductPrice()).quantity(product.getStockQuantity()).build();
    }
}
