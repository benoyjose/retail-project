package com.retail.payment.dto;

import com.retail.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentReference;
    private String orderNumber;
    private Long customerId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String failureReason;
    private Instant processedAt;
}

