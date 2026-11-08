package com.retail.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.common.enums.UserRole;
import com.retail.customer.config.SecurityConfig;
import com.retail.customer.dto.CustomerRegistrationRequest;
import com.retail.customer.dto.CustomerResponse;
import com.retail.customer.exception.CustomerAlreadyExistsException;
import com.retail.customer.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(SecurityConfig.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    @DisplayName("POST /api/v1/customers/register - Success 201 Created")
    void shouldRegisterCustomerSuccessfully() throws Exception {
        CustomerRegistrationRequest request = CustomerRegistrationRequest.builder()
                .email("clara.barton@example.com")
                .password("SecurePass123")
                .firstName("Clara")
                .lastName("Barton")
                .phone("+1-555-432-1098")
                .shippingAddress("789 Pine Road, City, 90210")
                .build();

        CustomerResponse response = CustomerResponse.builder()
                .id(101L)
                .email("clara.barton@example.com")
                .firstName("Clara")
                .lastName("Barton")
                .phone("+1-555-432-1098")
                .shippingAddress("789 Pine Road, City, 90210")
                .role(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build();

        when(customerService.registerCustomer(any(CustomerRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.email").value("clara.barton@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Clara"));
    }

    @Test
    @DisplayName("POST /api/v1/customers/register - Duplicate Email Returns 409 Conflict")
    void shouldReturn409WhenDuplicateEmail() throws Exception {
        CustomerRegistrationRequest request = CustomerRegistrationRequest.builder()
                .email("duplicate@example.com")
                .password("SecurePass123")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(customerService.registerCustomer(any(CustomerRegistrationRequest.class)))
                .thenThrow(new CustomerAlreadyExistsException("A customer with email 'duplicate@example.com' already exists."));

        mockMvc.perform(post("/api/v1/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Customer Email Conflict"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/v1/customers/register - Invalid Email Returns 400 Bad Request")
    void shouldReturn400WhenInvalidEmailFormat() throws Exception {
        CustomerRegistrationRequest request = CustomerRegistrationRequest.builder()
                .email("invalid-email-format")
                .password("SecurePass123")
                .firstName("John")
                .lastName("Doe")
                .build();

        mockMvc.perform(post("/api/v1/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} - Success 200 OK")
    void shouldGetCustomerById() throws Exception {
        CustomerResponse response = CustomerResponse.builder()
                .id(50L)
                .email("existing@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .role(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build();

        when(customerService.getCustomerById(50L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(50))
                .andExpect(jsonPath("$.data.email").value("existing@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/customers/login - Success 200 OK")
    void shouldLoginSuccessfully() throws Exception {
        com.retail.customer.dto.LoginRequest request = new com.retail.customer.dto.LoginRequest("existing@example.com", "Password@123");
        com.retail.customer.dto.AuthResponse response = com.retail.customer.dto.AuthResponse.builder()
                .accessToken("test-jwt-token")
                .refreshToken("test-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .build();

        when(customerService.login(any(com.retail.customer.dto.LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test-jwt-token"))
                .andExpect(jsonPath("$.data.expiresIn").value(900));
    }
}

