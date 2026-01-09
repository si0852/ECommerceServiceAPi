package kr.hhplus.be.server.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "stock_quantity")
    private Long stockQuantity;

    @Column(name = "product_price", precision = 10, scale = 2)
    private BigDecimal productPrice;

    @Column(name = "product_desc")
    private String productDesc;

    @Column(name = "status")
    private String status;

    @Builder
    public Product(Long productId, String productName, Long stockQuantity, BigDecimal productPrice,
                   String productDesc, String status) {
        this.productId = productId;
        this.productName = productName;
        this.stockQuantity = stockQuantity;
        this.productPrice = productPrice;
        this.productDesc = productDesc;
        this.status = status;
    }

    public void decreaseStock(Long quantity) {

        if (quantity == null || quantity <= 0) {throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
        }

        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(Long quantity) {
        this.stockQuantity += quantity;
    }

    public void updateStatus(String status) {
        this.status = status;
    }
}
