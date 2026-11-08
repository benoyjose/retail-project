package com.retail.support.service;

import com.retail.common.enums.SupportTicketStatus;
import com.retail.support.dto.SupportTicketCreateRequest;
import com.retail.support.dto.SupportTicketReplyRequest;
import com.retail.support.dto.SupportTicketResponse;

import java.util.List;

public interface SupportTicketService {

    SupportTicketResponse createTicket(Long customerId, String customerEmail, SupportTicketCreateRequest request);

    SupportTicketResponse getTicketByNumber(String ticketNumber);

    List<SupportTicketResponse> getCustomerTickets(Long customerId);

    List<SupportTicketResponse> getAllTickets(SupportTicketStatus status);

    SupportTicketResponse replyAndResolveTicket(String ticketNumber, SupportTicketReplyRequest request);
}

