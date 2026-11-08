package com.retail.campaign.service.impl;

import com.retail.campaign.dto.CampaignCreateRequest;
import com.retail.campaign.dto.CampaignResponse;
import com.retail.campaign.dto.CampaignStatusUpdateRequest;
import com.retail.campaign.entity.Campaign;
import com.retail.campaign.exception.CampaignNotFoundException;
import com.retail.campaign.exception.DuplicateCampaignException;
import com.retail.campaign.repository.CampaignRepository;
import com.retail.campaign.service.CampaignService;
import com.retail.common.enums.CampaignStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;

    @Override
    @Transactional
    public CampaignResponse createCampaign(CampaignCreateRequest request) {
        log.info("Creating promotional campaign with code: {}", request.getCampaignCode());

        if (campaignRepository.existsByCampaignCode(request.getCampaignCode())) {
            throw new DuplicateCampaignException("Campaign already exists with code: " + request.getCampaignCode());
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Campaign start date must be before end date");
        }

        CampaignStatus status = request.getStatus() != null ? request.getStatus() : CampaignStatus.SCHEDULED;

        Campaign campaign = Campaign.builder()
                .campaignCode(request.getCampaignCode().toUpperCase().trim())
                .title(request.getTitle())
                .description(request.getDescription())
                .discountPercentage(request.getDiscountPercentage())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(status)
                .applicableCategoryIds(request.getApplicableCategoryIds() != null ? new ArrayList<>(request.getApplicableCategoryIds()) : new ArrayList<>())
                .applicableSkus(request.getApplicableSkus() != null ? new ArrayList<>(request.getApplicableSkus()) : new ArrayList<>())
                .build();

        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaign created with id: {} and code: {}", saved.getId(), saved.getCampaignCode());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getCampaignByCode(String campaignCode) {
        Campaign campaign = campaignRepository.findByCampaignCode(campaignCode.toUpperCase().trim())
                .orElseThrow(() -> new CampaignNotFoundException("Campaign not found with code: " + campaignCode));
        return mapToResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException("Campaign not found with id: " + id));
        return mapToResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getAllCampaigns() {
        return campaignRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getActiveCampaigns() {
        return campaignRepository.findActiveCampaignsAt(Instant.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CampaignResponse updateCampaignStatus(String campaignCode, CampaignStatusUpdateRequest request) {
        Campaign campaign = campaignRepository.findByCampaignCode(campaignCode.toUpperCase().trim())
                .orElseThrow(() -> new CampaignNotFoundException("Campaign not found with code: " + campaignCode));

        campaign.setStatus(request.getStatus());
        Campaign updated = campaignRepository.save(campaign);
        log.info("Updated campaign {} status to {}", campaignCode, request.getStatus());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCampaign(Long id) {
        if (!campaignRepository.existsById(id)) {
            throw new CampaignNotFoundException("Campaign not found with id: " + id);
        }
        campaignRepository.deleteById(id);
        log.info("Deleted campaign with id: {}", id);
    }

    private CampaignResponse mapToResponse(Campaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .campaignCode(campaign.getCampaignCode())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .discountPercentage(campaign.getDiscountPercentage())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .status(campaign.getStatus())
                .active(campaign.isCurrentlyActive())
                .applicableCategoryIds(campaign.getApplicableCategoryIds())
                .applicableSkus(campaign.getApplicableSkus())
                .createdAt(campaign.getCreatedAt())
                .build();
    }
}

