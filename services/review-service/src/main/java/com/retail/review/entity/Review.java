package com.retail.review.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_sku", columnList = "product_sku"),
        @Index(name = "idx_reviews_customer_sku", columnList = "customer_id, product_sku")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 50)
    private String productSku;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 2000)
    private String reviewText;

    @Builder.Default
    private boolean verifiedPurchase = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}

