package com.retail.product.repository;

import com.retail.product.config.JpaConfig;
import com.retail.product.entity.Category;
import com.retail.product.entity.Product;
import com.retail.product.repository.specification.ProductSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronicsCategory;

    @BeforeEach
    void setUp() {
        electronicsCategory = categoryRepository.save(Category.builder()
                .name("Electronics")
                .code("ELEC")
                .description("Electronic gadgets and devices")
                .build());

        Product laptop = Product.builder()
                .sku("LAPTOP-PRO-15")
                .name("Pro Laptop 15-inch")
                .description("High performance developer laptop with 32GB RAM")
                .basePrice(new BigDecimal("1299.99"))
                .category(electronicsCategory)
                .brand("TechCorp")
                .active(true)
                .averageRating(4.8)
                .reviewCount(25)
                .createdAt(Instant.now())
                .build();

        Product mouse = Product.builder()
                .sku("MOUSE-WIRELESS-01")
                .name("Wireless Ergonomic Mouse")
                .description("Silent optical mouse with Bluetooth 5.0")
                .basePrice(new BigDecimal("49.99"))
                .category(electronicsCategory)
                .brand("TechCorp")
                .active(true)
                .averageRating(4.5)
                .reviewCount(110)
                .createdAt(Instant.now())
                .build();

        productRepository.save(laptop);
        productRepository.save(mouse);
    }

    @Test
    @DisplayName("Should find product by SKU")
    void shouldFindProductBySku() {
        Optional<Product> found = productRepository.findBySku("LAPTOP-PRO-15");
        assertTrue(found.isPresent());
        assertEquals("Pro Laptop 15-inch", found.get().getName());
        assertEquals(new BigDecimal("1299.99"), found.get().getBasePrice());
    }

    @Test
    @DisplayName("Should dynamically filter products by keyword using Specification")
    void shouldFilterProductsByKeyword() {
        Specification<Product> spec = ProductSpecification.filter(
                "developer", null, null, null, null, null);

        Page<Product> results = productRepository.findAll(spec, PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("LAPTOP-PRO-15", results.getContent().get(0).getSku());
    }

    @Test
    @DisplayName("Should filter products by price range")
    void shouldFilterProductsByPriceRange() {
        Specification<Product> spec = ProductSpecification.filter(
                null, null, null, new BigDecimal("10.00"), new BigDecimal("100.00"), null);

        Page<Product> results = productRepository.findAll(spec, PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("MOUSE-WIRELESS-01", results.getContent().get(0).getSku());
    }
}
