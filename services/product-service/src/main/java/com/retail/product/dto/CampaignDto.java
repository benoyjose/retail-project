package com.retail.product.dto;

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
public class CampaignDto {
    private Long id;
    private String campaignCode;
    private String title;
    private BigDecimal discountPercentage;
    private List<Long> applicableCategoryIds;
    private List<String> applicableSkus;
    private Instant startDate;
    private Instant endDate;
    private boolean active;
}

