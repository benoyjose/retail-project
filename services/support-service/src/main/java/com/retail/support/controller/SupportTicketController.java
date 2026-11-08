package com.retail.support.controller;

import com.retail.common.dto.ApiResponse;
import com.retail.common.enums.SupportTicketStatus;
import com.retail.support.dto.SupportTicketCreateRequest;
import com.retail.support.dto.SupportTicketReplyRequest;
import com.retail.support.dto.SupportTicketResponse;
import com.retail.support.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support/tickets")
@RequiredArgsConstructor
@Tag(name = "Customer Support", description = "Endpoints for submitting and managing support tickets (Use Case 9)")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    @Operation(summary = "Submit a support ticket", description = "Allows authenticated customers to submit issue tickets")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> createTicket(
            @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmailHeader,
            @Valid @RequestBody SupportTicketCreateRequest request) {

        SupportTicketResponse created = supportTicketService.createTicket(userIdHeader, userEmailHeader, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Support ticket submitted successfully"));
    }

    @GetMapping("/my-tickets")
    @Operation(summary = "Get current customer's tickets", description = "Retrieves tickets submitted by logged-in customer")
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>> getMyTickets(
            @RequestHeader("X-User-Id") Long customerId) {
        List<SupportTicketResponse> tickets = supportTicketService.getCustomerTickets(customerId);
        return ResponseEntity.ok(ApiResponse.ok(tickets, "Customer tickets retrieved successfully"));
    }

    @GetMapping("/{ticketNumber}")
    @Operation(summary = "Get ticket by number", description = "Retrieves ticket details and status by ticket number")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> getTicket(
            @PathVariable("ticketNumber") String ticketNumber) {
        SupportTicketResponse ticket = supportTicketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ApiResponse.ok(ticket, "Support ticket retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "List all tickets", description = "Support agents can retrieve tickets, optionally filtered by status")
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>> getAllTickets(
            @RequestParam(value = "status", required = false) SupportTicketStatus status) {
        List<SupportTicketResponse> tickets = supportTicketService.getAllTickets(status);
        return ResponseEntity.ok(ApiResponse.ok(tickets, "Support tickets retrieved successfully"));
    }

    @PutMapping("/{ticketNumber}/reply")
    @Operation(summary = "Reply and update ticket", description = "Support representatives add notes/reply and update ticket status")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> replyToTicket(
            @PathVariable("ticketNumber") String ticketNumber,
            @Valid @RequestBody SupportTicketReplyRequest request) {
        SupportTicketResponse updated = supportTicketService.replyAndResolveTicket(ticketNumber, request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Support ticket updated successfully"));
    }
}

