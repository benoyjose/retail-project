package com.retail.support.dto;

import com.retail.common.enums.SupportTicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketReplyRequest {

    private Long agentId;

    @NotBlank(message = "Agent notes or resolution reply is required")
    private String agentNotes;

    @NotNull(message = "Updated status is required")
    private SupportTicketStatus status;
}

