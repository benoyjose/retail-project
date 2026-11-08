package com.retail.support.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.common.enums.SupportTicketPriority;
import com.retail.common.enums.SupportTicketStatus;
import com.retail.support.dto.SupportTicketCreateRequest;
import com.retail.support.dto.SupportTicketReplyRequest;
import com.retail.support.dto.SupportTicketResponse;
import com.retail.support.service.SupportTicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SupportTicketController.class)
@ActiveProfiles("test")
class SupportTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupportTicketService supportTicketService;

    @Test
    @DisplayName("POST /api/v1/support/tickets - Should create support ticket")
    void shouldCreateTicket() throws Exception {
        SupportTicketCreateRequest request = SupportTicketCreateRequest.builder()
                .customerId(101L)
                .customerEmail("customer@test.com")
                .subject("Billing Discrepancy")
                .message("Overcharged on recent purchase checkout.")
                .priority(SupportTicketPriority.MEDIUM)
                .build();

        SupportTicketResponse response = SupportTicketResponse.builder()
                .id(1L)
                .ticketNumber("TCK-9998887776")
                .customerId(101L)
                .customerEmail("customer@test.com")
                .subject("Billing Discrepancy")
                .status(SupportTicketStatus.OPEN)
                .priority(SupportTicketPriority.MEDIUM)
                .createdAt(Instant.now())
                .build();

        when(supportTicketService.createTicket(any(), any(), any(SupportTicketCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/support/tickets")
                        .header("X-User-Id", 101L)
                        .header("X-User-Email", "customer@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").value("TCK-9998887776"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    @DisplayName("GET /api/v1/support/tickets/my-tickets - Should return tickets for logged in customer")
    void shouldGetMyTickets() throws Exception {
        SupportTicketResponse response = SupportTicketResponse.builder()
                .id(1L)
                .ticketNumber("TCK-9998887776")
                .customerId(101L)
                .subject("Billing Discrepancy")
                .status(SupportTicketStatus.OPEN)
                .build();

        when(supportTicketService.getCustomerTickets(101L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/support/tickets/my-tickets")
                        .header("X-User-Id", 101L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].ticketNumber").value("TCK-9998887776"));
    }

    @Test
    @DisplayName("PUT /api/v1/support/tickets/{ticketNumber}/reply - Should reply and resolve ticket")
    void shouldReplyToTicket() throws Exception {
        SupportTicketReplyRequest replyRequest = SupportTicketReplyRequest.builder()
                .agentId(50L)
                .agentNotes("Refund processed successfully")
                .status(SupportTicketStatus.RESOLVED)
                .build();

        SupportTicketResponse response = SupportTicketResponse.builder()
                .id(1L)
                .ticketNumber("TCK-9998887776")
                .customerId(101L)
                .status(SupportTicketStatus.RESOLVED)
                .agentNotes("Refund processed successfully")
                .build();

        when(supportTicketService.replyAndResolveTicket(eq("TCK-9998887776"), any(SupportTicketReplyRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/support/tickets/TCK-9998887776/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }
}

