package com.retail.support.service;

import com.retail.common.enums.SupportTicketPriority;
import com.retail.common.enums.SupportTicketStatus;
import com.retail.support.dto.SupportTicketCreateRequest;
import com.retail.support.dto.SupportTicketReplyRequest;
import com.retail.support.dto.SupportTicketResponse;
import com.retail.support.entity.SupportTicket;
import com.retail.support.exception.SupportTicketNotFoundException;
import com.retail.support.repository.SupportTicketRepository;
import com.retail.support.service.impl.SupportTicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @InjectMocks
    private SupportTicketServiceImpl supportTicketService;

    private SupportTicket ticket;
    private SupportTicketCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        ticket = SupportTicket.builder()
                .id(1L)
                .ticketNumber("TCK-ABC1234567")
                .customerId(101L)
                .customerEmail("customer@test.com")
                .subject("Damaged Item in Order")
                .message("The screen of the monitor arrived cracked.")
                .priority(SupportTicketPriority.HIGH)
                .status(SupportTicketStatus.OPEN)
                .createdAt(Instant.now())
                .build();

        createRequest = SupportTicketCreateRequest.builder()
                .customerId(101L)
                .customerEmail("customer@test.com")
                .subject("Damaged Item in Order")
                .message("The screen of the monitor arrived cracked.")
                .priority(SupportTicketPriority.HIGH)
                .build();
    }

    @Test
    @DisplayName("Should create support ticket successfully")
    void shouldCreateTicket() {
        when(supportTicketRepository.save(any(SupportTicket.class))).thenReturn(ticket);

        SupportTicketResponse response = supportTicketService.createTicket(101L, "customer@test.com", createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTicketNumber()).isEqualTo("TCK-ABC1234567");
        assertThat(response.getPriority()).isEqualTo(SupportTicketPriority.HIGH);
        assertThat(response.getStatus()).isEqualTo(SupportTicketStatus.OPEN);
        verify(supportTicketRepository, times(1)).save(any(SupportTicket.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when customerId is missing")
    void shouldThrowExceptionWhenCustomerIdMissing() {
        SupportTicketCreateRequest emptyRequest = SupportTicketCreateRequest.builder()
                .subject("Subject")
                .message("Message content here")
                .build();

        assertThatThrownBy(() -> supportTicketService.createTicket(null, null, emptyRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer ID is required");

        verify(supportTicketRepository, never()).save(any(SupportTicket.class));
    }

    @Test
    @DisplayName("Should retrieve ticket by ticketNumber")
    void shouldGetTicketByNumber() {
        when(supportTicketRepository.findByTicketNumber("TCK-ABC1234567")).thenReturn(Optional.of(ticket));

        SupportTicketResponse response = supportTicketService.getTicketByNumber("TCK-ABC1234567");

        assertThat(response).isNotNull();
        assertThat(response.getSubject()).isEqualTo("Damaged Item in Order");
    }

    @Test
    @DisplayName("Should throw SupportTicketNotFoundException when ticket does not exist")
    void shouldThrowExceptionWhenTicketNotFound() {
        when(supportTicketRepository.findByTicketNumber("TCK-UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supportTicketService.getTicketByNumber("TCK-UNKNOWN"))
                .isInstanceOf(SupportTicketNotFoundException.class);
    }

    @Test
    @DisplayName("Should reply to ticket and mark resolved")
    void shouldReplyAndResolveTicket() {
        when(supportTicketRepository.findByTicketNumber("TCK-ABC1234567")).thenReturn(Optional.of(ticket));
        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupportTicketReplyRequest replyRequest = SupportTicketReplyRequest.builder()
                .agentId(999L)
                .agentNotes("Replacement has been shipped with priority delivery.")
                .status(SupportTicketStatus.RESOLVED)
                .build();

        SupportTicketResponse response = supportTicketService.replyAndResolveTicket("TCK-ABC1234567", replyRequest);

        assertThat(response.getStatus()).isEqualTo(SupportTicketStatus.RESOLVED);
        assertThat(response.getAgentNotes()).isEqualTo("Replacement has been shipped with priority delivery.");
        assertThat(response.getAssignedAgentId()).isEqualTo(999L);
        assertThat(response.getResolvedAt()).isNotNull();
    }
}

