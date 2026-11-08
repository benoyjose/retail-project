package com.retail.campaign.service;

import com.retail.campaign.dto.CampaignCreateRequest;
import com.retail.campaign.dto.CampaignResponse;
import com.retail.campaign.dto.CampaignStatusUpdateRequest;

import java.util.List;

public interface CampaignService {

    CampaignResponse createCampaign(CampaignCreateRequest request);

    CampaignResponse getCampaignByCode(String campaignCode);

    CampaignResponse getCampaignById(Long id);

    List<CampaignResponse> getAllCampaigns();

    List<CampaignResponse> getActiveCampaigns();

    CampaignResponse updateCampaignStatus(String campaignCode, CampaignStatusUpdateRequest request);

    void deleteCampaign(Long id);
}

