package com.retail.support.dto;

import com.retail.common.enums.SupportTicketPriority;
import com.retail.common.enums.SupportTicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketResponse {
    private Long id;
    private String ticketNumber;
    private Long customerId;
    private String customerEmail;
    private String orderNumber;
    private String subject;
    private String message;
    private SupportTicketPriority priority;
    private SupportTicketStatus status;
    private Long assignedAgentId;
    private String agentNotes;
    private Instant resolvedAt;
    private Instant createdAt;
    private Instant updatedAt;
}

