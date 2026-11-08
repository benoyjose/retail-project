package com.retail.campaign.service;

import com.retail.campaign.dto.CampaignCreateRequest;
import com.retail.campaign.dto.CampaignResponse;
import com.retail.campaign.dto.CampaignStatusUpdateRequest;
import com.retail.campaign.entity.Campaign;
import com.retail.campaign.exception.CampaignNotFoundException;
import com.retail.campaign.exception.DuplicateCampaignException;
import com.retail.campaign.repository.CampaignRepository;
import com.retail.campaign.service.impl.CampaignServiceImpl;
import com.retail.common.enums.CampaignStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    private Campaign campaign;
    private CampaignCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        campaign = Campaign.builder()
                .id(1L)
                .campaignCode("WINTER2026")
                .title("Winter Carnival")
                .description("Year-end discount")
                .discountPercentage(BigDecimal.valueOf(20.00))
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(5, ChronoUnit.DAYS))
                .status(CampaignStatus.ACTIVE)
                .applicableCategoryIds(List.of(1L))
                .applicableSkus(List.of("SKU-100"))
                .createdAt(Instant.now())
                .build();

        createRequest = CampaignCreateRequest.builder()
                .campaignCode("WINTER2026")
                .title("Winter Carnival")
                .description("Year-end discount")
                .discountPercentage(BigDecimal.valueOf(20.00))
                .startDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(10, ChronoUnit.DAYS))
                .status(CampaignStatus.SCHEDULED)
                .applicableCategoryIds(List.of(1L))
                .applicableSkus(List.of("SKU-100"))
                .build();
    }

    @Test
    @DisplayName("Should successfully create a promotional campaign")
    void shouldCreateCampaign() {
        when(campaignRepository.existsByCampaignCode("WINTER2026")).thenReturn(false);
        when(campaignRepository.save(any(Campaign.class))).thenReturn(campaign);

        CampaignResponse response = campaignService.createCampaign(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCampaignCode()).isEqualTo("WINTER2026");
        assertThat(response.getDiscountPercentage()).isEqualByComparingTo(BigDecimal.valueOf(20.00));
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should throw DuplicateCampaignException when campaign code already exists")
    void shouldThrowDuplicateCampaignException() {
        when(campaignRepository.existsByCampaignCode("WINTER2026")).thenReturn(true);

        assertThatThrownBy(() -> campaignService.createCampaign(createRequest))
                .isInstanceOf(DuplicateCampaignException.class)
                .hasMessageContaining("WINTER2026");

        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should retrieve active campaigns")
    void shouldGetActiveCampaigns() {
        when(campaignRepository.findActiveCampaignsAt(any(Instant.class))).thenReturn(List.of(campaign));

        List<CampaignResponse> active = campaignService.getActiveCampaigns();

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getCampaignCode()).isEqualTo("WINTER2026");
        assertThat(active.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("Should retrieve campaign by code")
    void shouldGetCampaignByCode() {
        when(campaignRepository.findByCampaignCode("WINTER2026")).thenReturn(Optional.of(campaign));

        CampaignResponse response = campaignService.getCampaignByCode("WINTER2026");

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Winter Carnival");
    }

    @Test
    @DisplayName("Should update campaign status")
    void shouldUpdateCampaignStatus() {
        when(campaignRepository.findByCampaignCode("WINTER2026")).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignResponse response = campaignService.updateCampaignStatus("WINTER2026",
                new CampaignStatusUpdateRequest(CampaignStatus.PAUSED));

        assertThat(response.getStatus()).isEqualTo(CampaignStatus.PAUSED);
        verify(campaignRepository, times(1)).save(campaign);
    }
}

