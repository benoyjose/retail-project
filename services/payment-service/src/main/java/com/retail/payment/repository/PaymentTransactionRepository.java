package com.retail.payment.repository;

import com.retail.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByPaymentReference(String paymentReference);
    Optional<PaymentTransaction> findByOrderNumber(String orderNumber);
}

