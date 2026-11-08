package com.retail.support.service.impl;

import com.retail.common.enums.SupportTicketPriority;
import com.retail.common.enums.SupportTicketStatus;
import com.retail.support.dto.SupportTicketCreateRequest;
import com.retail.support.dto.SupportTicketReplyRequest;
import com.retail.support.dto.SupportTicketResponse;
import com.retail.support.entity.SupportTicket;
import com.retail.support.exception.SupportTicketNotFoundException;
import com.retail.support.repository.SupportTicketRepository;
import com.retail.support.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    @Override
    @Transactional
    public SupportTicketResponse createTicket(Long customerId, String customerEmail, SupportTicketCreateRequest request) {
        Long resolvedCustomerId = customerId != null ? customerId : request.getCustomerId();
        if (resolvedCustomerId == null) {
            throw new IllegalArgumentException("Customer ID is required to create a support ticket");
        }

        String resolvedEmail = customerEmail != null ? customerEmail : request.getCustomerEmail();

        String ticketNumber = "TCK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        SupportTicketPriority priority = request.getPriority() != null ? request.getPriority() : SupportTicketPriority.MEDIUM;

        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber(ticketNumber)
                .customerId(resolvedCustomerId)
                .customerEmail(resolvedEmail)
                .orderNumber(request.getOrderNumber())
                .subject(request.getSubject())
                .message(request.getMessage())
                .priority(priority)
                .status(SupportTicketStatus.OPEN)
                .build();

        SupportTicket saved = supportTicketRepository.save(ticket);
        log.info("Created support ticket {} for customer {}", saved.getTicketNumber(), resolvedCustomerId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicketByNumber(String ticketNumber) {
        SupportTicket ticket = supportTicketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new SupportTicketNotFoundException("Support ticket not found with number: " + ticketNumber));
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getCustomerTickets(Long customerId) {
        return supportTicketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getAllTickets(SupportTicketStatus status) {
        List<SupportTicket> tickets = (status != null) ?
                supportTicketRepository.findByStatusOrderByCreatedAtDesc(status) :
                supportTicketRepository.findAll();

        return tickets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupportTicketResponse replyAndResolveTicket(String ticketNumber, SupportTicketReplyRequest request) {
        SupportTicket ticket = supportTicketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new SupportTicketNotFoundException("Support ticket not found with number: " + ticketNumber));

        ticket.setAgentNotes(request.getAgentNotes());
        ticket.setStatus(request.getStatus());
        if (request.getAgentId() != null) {
            ticket.setAssignedAgentId(request.getAgentId());
        }

        if (request.getStatus() == SupportTicketStatus.RESOLVED || request.getStatus() == SupportTicketStatus.CLOSED) {
            ticket.setResolvedAt(Instant.now());
        }

        SupportTicket updated = supportTicketRepository.save(ticket);
        log.info("Support ticket {} updated to status {} by agent {}", ticketNumber, request.getStatus(), request.getAgentId());
        return mapToResponse(updated);
    }

    private SupportTicketResponse mapToResponse(SupportTicket ticket) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .customerId(ticket.getCustomerId())
                .customerEmail(ticket.getCustomerEmail())
                .orderNumber(ticket.getOrderNumber())
                .subject(ticket.getSubject())
                .message(ticket.getMessage())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .assignedAgentId(ticket.getAssignedAgentId())
                .agentNotes(ticket.getAgentNotes())
                .resolvedAt(ticket.getResolvedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}

