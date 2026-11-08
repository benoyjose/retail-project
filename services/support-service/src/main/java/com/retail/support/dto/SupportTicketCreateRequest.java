package com.retail.support.dto;

import com.retail.common.enums.SupportTicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketCreateRequest {

    private Long customerId;

    private String customerEmail;

    private String orderNumber;

    @NotBlank(message = "Subject is required")
    @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
    private String subject;

    @NotBlank(message = "Message details are required")
    @Size(min = 10, max = 4000, message = "Message must be between 10 and 4000 characters")
    private String message;

    private SupportTicketPriority priority;
}

