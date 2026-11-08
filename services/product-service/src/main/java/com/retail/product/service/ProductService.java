package com.retail.product.service;

import com.retail.product.dto.ProductCreateRequest;
import com.retail.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    Page<ProductResponse> searchProducts(
            String keyword,
            Long categoryId,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            Pageable pageable);

    ProductResponse getProductById(Long id);
    ProductResponse getProductBySku(String sku);
    ProductResponse createProduct(ProductCreateRequest request);
    java.util.List<ProductResponse> getRecommendations(Long categoryId, int limit);
}

