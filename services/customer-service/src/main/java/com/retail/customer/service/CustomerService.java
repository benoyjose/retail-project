package com.retail.customer.service;

import com.retail.customer.dto.*;

public interface CustomerService {
    CustomerResponse registerCustomer(CustomerRegistrationRequest request);
    CustomerResponse getCustomerById(Long customerId);
    CustomerResponse getCustomerByEmail(String email);
    CustomerResponse updateProfile(Long customerId, CustomerUpdateRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
}

