package com.retail.campaign.dto;

import com.retail.common.enums.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {
    private Long id;
    private String campaignCode;
    private String title;
    private String description;
    private BigDecimal discountPercentage;
    private List<Long> applicableCategoryIds;
    private List<String> applicableSkus;
    private Instant startDate;
    private Instant endDate;
    private CampaignStatus status;
    private boolean active;
    private Instant createdAt;
}

