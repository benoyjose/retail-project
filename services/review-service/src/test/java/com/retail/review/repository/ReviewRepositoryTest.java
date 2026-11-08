package com.retail.review.repository;

import com.retail.review.entity.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository repository;

    @Test
    @DisplayName("Should persist and find reviews by productSku")
    void shouldPersistAndFindByProductSku() {
        Review review1 = Review.builder()
                .customerId(10L)
                .productSku("SKU-LAPTOP-01")
                .rating(5)
                .reviewText("Outstanding build quality and battery life!")
                .verifiedPurchase(true)
                .build();

        Review review2 = Review.builder()
                .customerId(20L)
                .productSku("SKU-LAPTOP-01")
                .rating(4)
                .reviewText("Great machine, slightly heavy.")
                .verifiedPurchase(true)
                .build();

        repository.save(review1);
        repository.save(review2);

        List<Review> reviews = repository.findByProductSkuOrderByCreatedAtDesc("SKU-LAPTOP-01");
        assertEquals(2, reviews.size());

        Optional<Review> customerReview = repository.findByCustomerIdAndProductSku(10L, "SKU-LAPTOP-01");
        assertTrue(customerReview.isPresent());
        assertEquals(5, customerReview.get().getRating());
    }
}

