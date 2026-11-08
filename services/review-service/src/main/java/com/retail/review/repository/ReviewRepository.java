package com.retail.review.repository;

import com.retail.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductSkuOrderByCreatedAtDesc(String productSku);
    Optional<Review> findByCustomerIdAndProductSku(Long customerId, String productSku);
}

