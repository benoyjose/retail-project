package com.retail.customer.service.impl;

import com.retail.common.enums.UserRole;
import com.retail.common.event.CustomerRegisteredEvent;
import com.retail.customer.dto.CustomerRegistrationRequest;
import com.retail.customer.dto.CustomerResponse;
import com.retail.customer.dto.CustomerUpdateRequest;
import com.retail.customer.entity.Customer;
import com.retail.customer.exception.CustomerAlreadyExistsException;
import com.retail.customer.exception.CustomerNotFoundException;
import com.retail.customer.repository.CustomerRepository;
import com.retail.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final com.retail.customer.security.JwtTokenProvider jwtTokenProvider;

    @Value("${app.kafka.topics.customer-registered:customer-registered-topic}")
    private String customerRegisteredTopic;

    @Override
    @Transactional
    public CustomerResponse registerCustomer(CustomerRegistrationRequest request) {
        log.info("Processing registration request for email: {}", request.getEmail());

        if (customerRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            log.warn("Registration rejected. Email already exists: {}", request.getEmail());
            throw new CustomerAlreadyExistsException("A customer with email '" + request.getEmail() + "' already exists.");
        }

        Customer customer = Customer.builder()
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phone(request.getPhone())
                .shippingAddress(request.getShippingAddress())
                .role(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully with id: {}", savedCustomer.getId());

        // Publish event to Kafka
        publishRegistrationEvent(savedCustomer);

        return mapToResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));
        return mapToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateProfile(Long customerId, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        customer.setFirstName(request.getFirstName().trim());
        customer.setLastName(request.getLastName().trim());
        customer.setPhone(request.getPhone());
        customer.setShippingAddress(request.getShippingAddress());

        Customer updated = customerRepository.save(customer);
        log.info("Updated customer profile for id: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public com.retail.customer.dto.AuthResponse login(com.retail.customer.dto.LoginRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());
        Customer customer = customerRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String roles = customer.getRole() != null ? customer.getRole().name() : "ROLE_CUSTOMER";
        String accessToken = jwtTokenProvider.generateAccessToken(customer.getId(), customer.getEmail(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(customer.getId(), customer.getEmail());

        log.info("Customer {} successfully authenticated with JWT", customer.getId());
        return com.retail.customer.dto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L) // 15 mins (NFR 5.3)
                .customer(mapToResponse(customer))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public com.retail.customer.dto.AuthResponse refreshToken(com.retail.customer.dto.RefreshTokenRequest request) {
        Long userId = jwtTokenProvider.extractUserId(request.getRefreshToken());
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + userId));

        String roles = customer.getRole() != null ? customer.getRole().name() : "ROLE_CUSTOMER";
        String newAccessToken = jwtTokenProvider.generateAccessToken(customer.getId(), customer.getEmail(), roles);

        return com.retail.customer.dto.AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(900L)
                .customer(mapToResponse(customer))
                .build();
    }

    private void publishRegistrationEvent(Customer customer) {
        try {
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId(customer.getId())
                    .email(customer.getEmail())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .registeredAt(Instant.now())
                    .build();

            kafkaTemplate.send(customerRegisteredTopic, customer.getEmail(), event);
            log.info("Published CustomerRegisteredEvent to topic '{}' for customerId: {}", customerRegisteredTopic, customer.getId());
        } catch (Exception e) {
            log.error("Failed to publish CustomerRegisteredEvent to Kafka for customerId: {}", customer.getId(), e);
            // Do not fail the registration transaction if messaging broker is temporarily lagging
        }
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .shippingAddress(customer.getShippingAddress())
                .role(customer.getRole())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}

