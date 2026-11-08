package com.retail.customer.service;

import com.retail.common.enums.UserRole;
import com.retail.customer.dto.CustomerRegistrationRequest;
import com.retail.customer.dto.CustomerResponse;
import com.retail.customer.entity.Customer;
import com.retail.customer.exception.CustomerAlreadyExistsException;
import com.retail.customer.exception.CustomerNotFoundException;
import com.retail.customer.repository.CustomerRepository;
import com.retail.customer.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private com.retail.customer.security.JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(customerService, "customerRegisteredTopic", "customer-registered-topic");
    }

    @Test
    @DisplayName("Should successfully register a new customer and publish Kafka event")
    void shouldRegisterNewCustomer() {
        CustomerRegistrationRequest request = CustomerRegistrationRequest.builder()
                .email("test.user@example.com")
                .password("Password@123")
                .firstName("Test")
                .lastName("User")
                .phone("+1-555-987-6543")
                .shippingAddress("456 Oak Avenue")
                .build();

        Customer savedCustomer = Customer.builder()
                .id(1L)
                .email("test.user@example.com")
                .passwordHash("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .phone("+1-555-987-6543")
                .shippingAddress("456 Oak Avenue")
                .role(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build();

        when(customerRepository.existsByEmail("test.user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponse response = customerService.registerCustomer(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test.user@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertTrue(response.isActive());

        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(kafkaTemplate, times(1)).send(eq("customer-registered-topic"), eq("test.user@example.com"), any());
    }

    @Test
    @DisplayName("Should throw CustomerAlreadyExistsException when registering with duplicate email")
    void shouldThrowWhenDuplicateEmail() {
        CustomerRegistrationRequest request = CustomerRegistrationRequest.builder()
                .email("existing@example.com")
                .password("Password@123")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(customerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(CustomerAlreadyExistsException.class, () -> customerService.registerCustomer(request));

        verify(customerRepository, never()).save(any(Customer.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer ID does not exist")
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(999L));
    }

    @Test
    @DisplayName("Should successfully authenticate customer and generate JWT tokens")
    void shouldLoginSuccessfully() {
        Customer customer = Customer.builder()
                .id(1L)
                .email("test.user@example.com")
                .passwordHash("hashedSecret")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build();

        when(customerRepository.findByEmail("test.user@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("Password@123", "hashedSecret")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(1L, "test.user@example.com", "ROLE_CUSTOMER")).thenReturn("dummy-access-token");
        when(jwtTokenProvider.generateRefreshToken(1L, "test.user@example.com")).thenReturn("dummy-refresh-token");

        com.retail.customer.dto.LoginRequest loginRequest = new com.retail.customer.dto.LoginRequest("test.user@example.com", "Password@123");
        com.retail.customer.dto.AuthResponse authResponse = customerService.login(loginRequest);

        assertNotNull(authResponse);
        assertEquals("dummy-access-token", authResponse.getAccessToken());
        assertEquals("dummy-refresh-token", authResponse.getRefreshToken());
        assertEquals(900L, authResponse.getExpiresIn());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when password is wrong")
    void shouldThrowWhenPasswordWrong() {
        Customer customer = Customer.builder()
                .id(1L)
                .email("test.user@example.com")
                .passwordHash("hashedSecret")
                .build();

        when(customerRepository.findByEmail("test.user@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("WrongPassword", "hashedSecret")).thenReturn(false);

        com.retail.customer.dto.LoginRequest loginRequest = new com.retail.customer.dto.LoginRequest("test.user@example.com", "WrongPassword");
        assertThrows(IllegalArgumentException.class, () -> customerService.login(loginRequest));
    }
}

