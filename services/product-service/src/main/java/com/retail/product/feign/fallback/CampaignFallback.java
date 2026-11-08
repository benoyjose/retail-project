package com.retail.product.feign.fallback;

import com.retail.product.dto.CampaignDto;
import com.retail.product.feign.CampaignFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class CampaignFallback implements CampaignFeignClient {

    @Override
    public List<CampaignDto> getActiveCampaigns() {
        log.warn("Campaign Service is currently unavailable or circuit is open. Falling back to base catalog pricing.");
        return Collections.emptyList();
    }
}

