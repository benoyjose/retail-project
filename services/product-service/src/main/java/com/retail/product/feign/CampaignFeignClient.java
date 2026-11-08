package com.retail.product.feign;

import com.retail.product.dto.CampaignDto;
import com.retail.product.feign.fallback.CampaignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "campaign-service", fallback = CampaignFallback.class)
public interface CampaignFeignClient {

    @GetMapping("/api/v1/campaigns/active")
    List<CampaignDto> getActiveCampaigns();
}

