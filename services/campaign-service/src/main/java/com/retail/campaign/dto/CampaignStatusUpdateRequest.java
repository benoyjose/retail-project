package com.retail.campaign.dto;

import com.retail.common.enums.CampaignStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private CampaignStatus status;
}

