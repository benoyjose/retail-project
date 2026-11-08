package com.retail.product.service;

import com.retail.product.dto.CampaignDto;
import com.retail.product.dto.ProductCreateRequest;
import com.retail.product.dto.ProductResponse;
import com.retail.product.entity.Category;
import com.retail.product.entity.Product;
import com.retail.product.feign.CampaignFeignClient;
import com.retail.product.repository.CategoryRepository;
import com.retail.product.repository.ProductRepository;
import com.retail.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CampaignFeignClient campaignFeignClient;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category electronicsCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        electronicsCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .code("ELEC")
                .build();

        testProduct = Product.builder()
                .id(100L)
                .sku("PHONE-XYZ-01")
                .name("Smart Phone XYZ")
                .description("Latest smartphone model")
                .basePrice(new BigDecimal("1000.00"))
                .category(electronicsCategory)
                .brand("MobileTech")
                .active(true)
                .averageRating(4.6)
                .reviewCount(50)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should apply campaign discount dynamically when active campaign exists")
    void shouldApplyCampaignDiscount() {
        CampaignDto campaign = CampaignDto.builder()
                .id(1L)
                .campaignCode("SUMMER20")
                .title("Summer 20% Off Electronics")
                .discountPercentage(new BigDecimal("20.00"))
                .applicableCategoryIds(List.of(1L))
                .active(true)
                .build();

        when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(testProduct)));
        when(campaignFeignClient.getActiveCampaigns()).thenReturn(List.of(campaign));

        Page<ProductResponse> page = productService.searchProducts(
                "Phone", null, null, null, null, null, PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());

        ProductResponse response = page.getContent().get(0);
        assertEquals(new BigDecimal("1000.00"), response.getBasePrice());
        // 20% discount on $1000 = $800.00
        assertEquals(new BigDecimal("800.00"), response.getPromotionalPrice());
        assertEquals(new BigDecimal("20.00"), response.getDiscountPercentage());
        assertEquals("Summer 20% Off Electronics", response.getCampaignApplied());
    }

    @Test
    @DisplayName("Should fallback gracefully to basePrice when no campaigns are active")
    void shouldFallbackToBasePriceWhenNoCampaigns() {
        when(productRepository.findBySku("PHONE-XYZ-01")).thenReturn(Optional.of(testProduct));
        when(campaignFeignClient.getActiveCampaigns()).thenReturn(Collections.emptyList());

        ProductResponse response = productService.getProductBySku("PHONE-XYZ-01");

        assertNotNull(response);
        assertEquals(new BigDecimal("1000.00"), response.getBasePrice());
        assertEquals(new BigDecimal("1000.00"), response.getPromotionalPrice());
        assertEquals(BigDecimal.ZERO, response.getDiscountPercentage());
        assertNull(response.getCampaignApplied());
    }

    @Test
    @DisplayName("Should create a new product successfully")
    void shouldCreateNewProduct() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .sku("TABLET-TAB-10")
                .name("Tablet 10-inch")
                .basePrice(new BigDecimal("350.00"))
                .categoryId(1L)
                .brand("TechCorp")
                .build();

        when(productRepository.existsBySku("TABLET-TAB-10")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronicsCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        verify(productRepository, times(1)).save(any(Product.class));
    }
}

