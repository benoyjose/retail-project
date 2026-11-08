package com.retail.payment.service;

import com.retail.payment.dto.PaymentProcessRequest;
import com.retail.payment.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentProcessRequest request);
    PaymentResponse getPaymentByOrderNumber(String orderNumber);
    PaymentResponse getPaymentByReference(String paymentReference);
}

