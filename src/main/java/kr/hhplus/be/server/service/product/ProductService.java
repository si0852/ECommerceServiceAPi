package kr.hhplus.be.server.service.product;

import jakarta.persistence.EntityNotFoundException;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.presentation.dto.response.ProductResponse;
import kr.hhplus.be.server.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product decreaseStock(Long productId, Long quantity) {
        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다."));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        product.decreaseStock(quantity);

        return product;
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다."));

        return ProductResponse.from(product);
    }
}
