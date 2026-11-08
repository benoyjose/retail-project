package com.retail.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.review.dto.ProductReviewSummary;
import com.retail.review.dto.ReviewCreateRequest;
import com.retail.review.dto.ReviewResponse;
import com.retail.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @Test
    @DisplayName("POST /api/v1/reviews - Success 201 Created")
    void shouldSubmitReview() throws Exception {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .productSku("SKU-LAPTOP-01")
                .rating(5)
                .reviewText("Incredible machine!")
                .build();

        ReviewResponse response = ReviewResponse.builder()
                .id(1L)
                .customerId(10L)
                .productSku("SKU-LAPTOP-01")
                .rating(5)
                .reviewText("Incredible machine!")
                .verifiedPurchase(true)
                .build();

        when(reviewService.submitReview(eq(10L), any(ReviewCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.verifiedPurchase").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/reviews/product/{sku} - Success 200 OK")
    void shouldGetProductReviews() throws Exception {
        ProductReviewSummary summary = ProductReviewSummary.builder()
                .productSku("SKU-LAPTOP-01")
                .averageRating(4.8)
                .totalReviews(10)
                .reviews(List.of())
                .build();

        when(reviewService.getProductReviews("SKU-LAPTOP-01")).thenReturn(summary);

        mockMvc.perform(get("/api/v1/reviews/product/SKU-LAPTOP-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.averageRating").value(4.8))
                .andExpect(jsonPath("$.data.totalReviews").value(10));
    }
}

