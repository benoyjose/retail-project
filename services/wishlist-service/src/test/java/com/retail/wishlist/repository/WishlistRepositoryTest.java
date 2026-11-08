package com.retail.wishlist.repository;

import com.retail.wishlist.entity.WishlistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class WishlistRepositoryTest {

    @Autowired
    private WishlistRepository wishlistRepository;

    private WishlistItem item;

    @BeforeEach
    void setUp() {
        wishlistRepository.deleteAll();

        item = WishlistItem.builder()
                .customerId(100L)
                .productSku("SKU-LAPTOP-01")
                .productName("Gaming Laptop Pro")
                .unitPrice(BigDecimal.valueOf(1499.99))
                .imageUrl("https://images.example.com/laptop.jpg")
                .build();
    }

    @Test
    @DisplayName("Should save and find item by customerId and productSku")
    void shouldSaveAndFindByCustomerAndSku() {
        WishlistItem saved = wishlistRepository.save(item);
        assertThat(saved.getId()).isNotNull();

        Optional<WishlistItem> found = wishlistRepository.findByCustomerIdAndProductSku(100L, "SKU-LAPTOP-01");
        assertThat(found).isPresent();
        assertThat(found.get().getProductName()).isEqualTo("Gaming Laptop Pro");
    }

    @Test
    @DisplayName("Should retrieve items by customerId ordered by addedAt desc")
    void shouldFindItemsByCustomerId() {
        wishlistRepository.save(item);

        List<WishlistItem> items = wishlistRepository.findByCustomerIdOrderByAddedAtDesc(100L);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getProductSku()).isEqualTo("SKU-LAPTOP-01");
    }

    @Test
    @DisplayName("Should check if item exists for customer and SKU")
    void shouldCheckIfExists() {
        wishlistRepository.save(item);

        assertThat(wishlistRepository.existsByCustomerIdAndProductSku(100L, "SKU-LAPTOP-01")).isTrue();
        assertThat(wishlistRepository.existsByCustomerIdAndProductSku(100L, "SKU-UNKNOWN")).isFalse();
    }
}

