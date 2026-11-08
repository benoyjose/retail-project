package com.retail.wishlist.repository;

import com.retail.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByCustomerIdOrderByAddedAtDesc(Long customerId);

    Optional<WishlistItem> findByCustomerIdAndProductSku(Long customerId, String productSku);

    boolean existsByCustomerIdAndProductSku(Long customerId, String productSku);

    void deleteByCustomerIdAndProductSku(Long customerId, String productSku);

    void deleteByCustomerId(Long customerId);
}

