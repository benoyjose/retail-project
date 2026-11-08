package com.retail.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.product.dto.ProductCreateRequest;
import com.retail.product.dto.ProductResponse;
import com.retail.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/v1/products/search - Success 200 OK")
    void shouldSearchProductsSuccessfully() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .sku("SKU-LAPTOP-01")
                .name("Developer Laptop")
                .basePrice(new BigDecimal("1200.00"))
                .promotionalPrice(new BigDecimal("1080.00"))
                .discountPercentage(new BigDecimal("10.00"))
                .active(true)
                .build();

        when(productService.searchProducts(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/products/search")
                        .param("keyword", "laptop")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].sku").value("SKU-LAPTOP-01"))
                .andExpect(jsonPath("$.data.content[0].basePrice").value(1200.00))
                .andExpect(jsonPath("$.data.content[0].promotionalPrice").value(1080.00));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Success 200 OK")
    void shouldGetProductById() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(5L)
                .sku("SKU-MOUSE-01")
                .name("Ergonomic Mouse")
                .basePrice(new BigDecimal("50.00"))
                .promotionalPrice(new BigDecimal("50.00"))
                .build();

        when(productService.getProductById(5L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.sku").value("SKU-MOUSE-01"));
    }

    @Test
    @DisplayName("POST /api/v1/products - Success 201 Created")
    void shouldCreateProductSuccessfully() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .sku("SKU-KEYBOARD-01")
                .name("Mechanical Keyboard")
                .basePrice(new BigDecimal("89.99"))
                .categoryId(1L)
                .brand("KeyTech")
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(10L)
                .sku("SKU-KEYBOARD-01")
                .name("Mechanical Keyboard")
                .basePrice(new BigDecimal("89.99"))
                .promotionalPrice(new BigDecimal("89.99"))
                .build();

        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("SKU-KEYBOARD-01"));
    }
}

