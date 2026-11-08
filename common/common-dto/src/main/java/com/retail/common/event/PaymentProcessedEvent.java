package com.retail.common.event;

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
public class PaymentProcessedEvent {
    private String orderNumber;
    private String paymentReference;
    private BigDecimal amount;
    private PaymentStatus status;
    private String failureReason;
    private Instant processedAt;
}

