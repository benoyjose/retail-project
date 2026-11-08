package com.retail.campaign.controller;

import com.retail.campaign.dto.CampaignCreateRequest;
import com.retail.campaign.dto.CampaignResponse;
import com.retail.campaign.dto.CampaignStatusUpdateRequest;
import com.retail.campaign.service.CampaignService;
import com.retail.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaign Management", description = "Endpoints for managing promotional campaigns and marketing discounts (Use Case 8)")
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @Operation(summary = "Create promotional campaign", description = "Marketing managers create campaigns with discount rates, schedules, and targeted SKUs/categories")
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(@Valid @RequestBody CampaignCreateRequest request) {
        CampaignResponse created = campaignService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Campaign created successfully"));
    }

    @GetMapping
    @Operation(summary = "List all campaigns", description = "Retrieves all campaigns")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getAllCampaigns() {
        List<CampaignResponse> campaigns = campaignService.getAllCampaigns();
        return ResponseEntity.ok(ApiResponse.ok(campaigns, "Campaigns retrieved successfully"));
    }

    @GetMapping("/active")
    @Operation(summary = "List active campaigns", description = "Returns active campaigns for product pricing and Feign client consumers")
    public ResponseEntity<List<CampaignResponse>> getActiveCampaigns() {
        List<CampaignResponse> activeCampaigns = campaignService.getActiveCampaigns();
        return ResponseEntity.ok(activeCampaigns);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get campaign by code", description = "Retrieves details of a specific promotional campaign by code")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaignByCode(@PathVariable("code") String code) {
        CampaignResponse campaign = campaignService.getCampaignByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(campaign, "Campaign retrieved successfully"));
    }

    @PutMapping("/{code}/status")
    @Operation(summary = "Update campaign status", description = "Activates, schedules, pauses, or expires a promotional campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateStatus(
            @PathVariable("code") String code,
            @Valid @RequestBody CampaignStatusUpdateRequest request) {
        CampaignResponse updated = campaignService.updateCampaignStatus(code, request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Campaign status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete campaign", description = "Deletes a campaign by ID")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable("id") Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Campaign deleted successfully"));
    }
}
