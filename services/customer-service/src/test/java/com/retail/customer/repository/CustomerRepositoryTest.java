package com.retail.customer.repository;

import com.retail.common.enums.UserRole;
import com.retail.customer.config.JpaConfig;
import com.retail.customer.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Should successfully persist and retrieve customer by email")
    void shouldPersistAndFindCustomerByEmail() {
        Customer customer = Customer.builder()
                .email("alex.smith@example.com")
                .passwordHash("$2a$12$hashedPasswordPlaceholder")
                .firstName("Alex")
                .lastName("Smith")
                .phone("+1-555-123-4567")
                .shippingAddress("123 Maple Street, City, 10001")
                .role(UserRole.ROLE_CUSTOMER)
                .active(true)
                .build();

        Customer saved = customerRepository.save(customer);
        assertNotNull(saved.getId());

        Optional<Customer> found = customerRepository.findByEmail("alex.smith@example.com");
        assertTrue(found.isPresent());
        assertEquals("Alex", found.get().getFirstName());
        assertEquals("+1-555-123-4567", found.get().getPhone());
    }

    @Test
    @DisplayName("Should verify existsByEmail correctly")
    void shouldVerifyExistsByEmail() {
        Customer customer = Customer.builder()
                .email("jane.doe@example.com")
                .passwordHash("secretHash")
                .firstName("Jane")
                .lastName("Doe")
                .build();

        customerRepository.save(customer);

        assertTrue(customerRepository.existsByEmail("jane.doe@example.com"));
        assertFalse(customerRepository.existsByEmail("nonexistent@example.com"));
    }
}
