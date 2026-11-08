package com.retail.cart.repository;

import com.retail.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomerId(Long customerId);
    Optional<CartItem> findByCustomerIdAndProductSku(Long customerId, String productSku);
    void deleteByCustomerId(Long customerId);
    void deleteByCustomerIdAndProductSku(Long customerId, String productSku);
}

