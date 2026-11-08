package com.retail.customer.controller;

import com.retail.common.dto.ApiResponse;
import com.retail.customer.dto.CustomerRegistrationRequest;
import com.retail.customer.dto.CustomerResponse;
import com.retail.customer.dto.CustomerUpdateRequest;
import com.retail.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Customer Registration and Profile APIs (Use Case 1)")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Register a new customer account")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerResponse>> registerCustomer(
            @Valid @RequestBody CustomerRegistrationRequest request) {
        log.info("REST request to register customer with email: {}", request.getEmail());
        CustomerResponse response = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Customer registered successfully"));
    }

    @Operation(summary = "Authenticate customer and generate 15-minute JWT (NFR 5.3)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<com.retail.customer.dto.AuthResponse>> login(
            @Valid @RequestBody com.retail.customer.dto.LoginRequest request) {
        com.retail.customer.dto.AuthResponse response = customerService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Authentication successful"));
    }

    @Operation(summary = "Refresh expired JWT access token")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<com.retail.customer.dto.AuthResponse>> refreshToken(
            @Valid @RequestBody com.retail.customer.dto.RefreshTokenRequest request) {
        com.retail.customer.dto.AuthResponse response = customerService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Token refreshed successfully"));
    }

    @Operation(summary = "Get current authenticated customer profile")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        
        CustomerResponse response;
        if (userId != null && !userId.isBlank()) {
            response = customerService.getCustomerById(Long.parseLong(userId));
        } else if (userEmail != null && !userEmail.isBlank()) {
            response = customerService.getCustomerByEmail(userEmail);
        } else {
            // Default demo customer for direct non-gateway calls
            response = customerService.getCustomerById(1L);
        }
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Get customer details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable("id") Long id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Update authenticated customer profile")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateProfile(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerResponse response = customerService.updateProfile(Long.parseLong(userId), request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Profile updated successfully"));
    }
}
