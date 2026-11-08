package com.retail.support.repository;

import com.retail.common.enums.SupportTicketPriority;
import com.retail.common.enums.SupportTicketStatus;
import com.retail.support.entity.SupportTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SupportTicketRepositoryTest {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    private SupportTicket ticket;

    @BeforeEach
    void setUp() {
        supportTicketRepository.deleteAll();

        ticket = SupportTicket.builder()
                .ticketNumber("TCK-1234567890")
                .customerId(101L)
                .customerEmail("customer@example.com")
                .orderNumber("ORD-20261108-ABCD")
                .subject("Late Delivery Issue")
                .message("My package has not arrived within the estimated delivery window.")
                .priority(SupportTicketPriority.HIGH)
                .status(SupportTicketStatus.OPEN)
                .build();
    }

    @Test
    @DisplayName("Should save and find ticket by ticketNumber")
    void shouldSaveAndFindByTicketNumber() {
        SupportTicket saved = supportTicketRepository.save(ticket);
        assertThat(saved.getId()).isNotNull();

        Optional<SupportTicket> found = supportTicketRepository.findByTicketNumber("TCK-1234567890");
        assertThat(found).isPresent();
        assertThat(found.get().getSubject()).isEqualTo("Late Delivery Issue");
        assertThat(found.get().getCustomerId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("Should find tickets by customerId ordered by createdAt desc")
    void shouldFindByCustomerId() {
        supportTicketRepository.save(ticket);

        List<SupportTicket> list = supportTicketRepository.findByCustomerIdOrderByCreatedAtDesc(101L);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getTicketNumber()).isEqualTo("TCK-1234567890");
    }

    @Test
    @DisplayName("Should find tickets by status")
    void shouldFindByStatus() {
        supportTicketRepository.save(ticket);

        List<SupportTicket> openTickets = supportTicketRepository.findByStatusOrderByCreatedAtDesc(SupportTicketStatus.OPEN);
        assertThat(openTickets).hasSize(1);

        List<SupportTicket> closedTickets = supportTicketRepository.findByStatusOrderByCreatedAtDesc(SupportTicketStatus.CLOSED);
        assertThat(closedTickets).isEmpty();
    }
}

