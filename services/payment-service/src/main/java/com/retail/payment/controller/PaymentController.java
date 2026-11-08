package com.retail.payment.controller;

import com.retail.common.dto.ApiResponse;
import com.retail.payment.dto.PaymentProcessRequest;
import com.retail.payment.dto.PaymentResponse;
import com.retail.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Processing", description = "Payment Gateway Simulation & Transaction Audit APIs (Use Case 4)")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Process and record a payment transaction")
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentProcessRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Payment processed successfully"));
    }

    @Operation(summary = "Get payment transaction details by order number")
    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(
            @PathVariable("orderNumber") String orderNumber) {
        PaymentResponse response = paymentService.getPaymentByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Get payment transaction details by reference")
    @GetMapping("/ref/{paymentReference}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReference(
            @PathVariable("paymentReference") String paymentReference) {
        PaymentResponse response = paymentService.getPaymentByReference(paymentReference);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

