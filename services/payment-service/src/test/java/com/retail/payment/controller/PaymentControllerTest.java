package com.retail.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.common.enums.PaymentStatus;
import com.retail.payment.dto.PaymentProcessRequest;
import com.retail.payment.dto.PaymentResponse;
import com.retail.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("POST /api/v1/payments/process - Success 200 OK")
    void shouldProcessPaymentSuccessfully() throws Exception {
        PaymentProcessRequest request = PaymentProcessRequest.builder()
                .orderNumber("ORD-1001")
                .customerId(10L)
                .amount(new BigDecimal("150.00"))
                .paymentMethod("CREDIT_CARD")
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .paymentReference("PAY-12345")
                .orderNumber("ORD-1001")
                .customerId(10L)
                .amount(new BigDecimal("150.00"))
                .paymentMethod("CREDIT_CARD")
                .status(PaymentStatus.SUCCESS)
                .processedAt(Instant.now())
                .build();

        when(paymentService.processPayment(any(PaymentProcessRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentReference").value("PAY-12345"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/order/{orderNumber} - Success 200 OK")
    void shouldGetPaymentByOrderNumber() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .paymentReference("PAY-999")
                .orderNumber("ORD-999")
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentService.getPaymentByOrderNumber("ORD-999")).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/order/ORD-999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentReference").value("PAY-999"));
    }
}

