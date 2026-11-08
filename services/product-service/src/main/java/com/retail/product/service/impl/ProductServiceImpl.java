package com.retail.product.service.impl;

import com.retail.product.dto.CampaignDto;
import com.retail.product.dto.ProductCreateRequest;
import com.retail.product.dto.ProductResponse;
import com.retail.product.entity.Category;
import com.retail.product.entity.Product;
import com.retail.product.exception.CategoryNotFoundException;
import com.retail.product.exception.ProductNotFoundException;
import com.retail.product.feign.CampaignFeignClient;
import com.retail.product.repository.CategoryRepository;
import com.retail.product.repository.ProductRepository;
import com.retail.product.repository.specification.ProductSpecification;
import com.retail.product.service.ProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CampaignFeignClient campaignFeignClient;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            String keyword,
            Long categoryId,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            Pageable pageable) {

        log.info("Searching products with keyword='{}', categoryId={}, brand='{}'", keyword, categoryId, brand);

        Specification<Product> spec = ProductSpecification.filter(
                keyword, categoryId, brand, minPrice, maxPrice, minRating);

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<CampaignDto> activeCampaigns = getActiveCampaignsWithCircuitBreaker();

        return productPage.map(product -> mapToProductResponse(product, activeCampaigns));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        List<CampaignDto> activeCampaigns = getActiveCampaignsWithCircuitBreaker();
        return mapToProductResponse(product, activeCampaigns);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));

        List<CampaignDto> activeCampaigns = getActiveCampaignsWithCircuitBreaker();
        return mapToProductResponse(product, activeCampaigns);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + request.getSku() + "' already exists.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .sku(request.getSku().trim().toUpperCase())
                .name(request.getName().trim())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .category(category)
                .brand(request.getBrand())
                .imageUrl(request.getImageUrl())
                .active(true)
                .averageRating(0.0)
                .reviewCount(0)
                .build();

        Product saved = productRepository.save(product);
        return mapToProductResponse(saved, Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getRecommendations(Long categoryId, int limit) {
        log.info("Generating intelligent product recommendations for category: {}, limit: {}", categoryId, limit);
        List<Product> products = (categoryId != null && categoryId > 0)
                ? productRepository.findTop10ByCategoryIdAndActiveTrueOrderByAverageRatingDesc(categoryId)
                : productRepository.findTop10ByActiveTrueOrderByAverageRatingDesc();

        List<CampaignDto> activeCampaigns = getActiveCampaignsWithCircuitBreaker();
        return products.stream()
                .limit(limit > 0 ? limit : 6)
                .map(product -> mapToProductResponse(product, activeCampaigns))
                .collect(Collectors.toList());
    }

    @CircuitBreaker(name = "campaignService", fallbackMethod = "fallbackActiveCampaigns")
    public List<CampaignDto> getActiveCampaignsWithCircuitBreaker() {
        try {
            return campaignFeignClient.getActiveCampaigns();
        } catch (Exception e) {
            log.warn("Direct Feign call to campaign service failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<CampaignDto> fallbackActiveCampaigns(Throwable throwable) {
        log.warn("Resilience4j Circuit Breaker triggered for Campaign Service. Reason: {}", throwable.getMessage());
        return Collections.emptyList();
    }

    private ProductResponse mapToProductResponse(Product product, List<CampaignDto> activeCampaigns) {
        BigDecimal basePrice = product.getBasePrice();
        BigDecimal effectivePrice = basePrice;
        BigDecimal discountPercent = BigDecimal.ZERO;
        String campaignApplied = null;

        if (activeCampaigns != null && !activeCampaigns.isEmpty()) {
            for (CampaignDto campaign : activeCampaigns) {
                boolean matchesCategory = campaign.getApplicableCategoryIds() != null &&
                        campaign.getApplicableCategoryIds().contains(product.getCategory().getId());
                boolean matchesSku = campaign.getApplicableSkus() != null &&
                        campaign.getApplicableSkus().contains(product.getSku());

                if (matchesCategory || matchesSku) {
                    discountPercent = campaign.getDiscountPercentage();
                    campaignApplied = campaign.getTitle();
                    BigDecimal discountFactor = BigDecimal.valueOf(100).subtract(discountPercent)
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                    effectivePrice = basePrice.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
                    break;
                }
            }
        }

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(basePrice)
                .promotionalPrice(effectivePrice)
                .discountPercentage(discountPercent)
                .campaignApplied(campaignApplied)
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .brand(product.getBrand())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .build();
    }
}

