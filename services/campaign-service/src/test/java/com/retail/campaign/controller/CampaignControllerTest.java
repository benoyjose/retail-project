package com.retail.campaign.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.campaign.dto.CampaignCreateRequest;
import com.retail.campaign.dto.CampaignResponse;
import com.retail.campaign.dto.CampaignStatusUpdateRequest;
import com.retail.campaign.service.CampaignService;
import com.retail.common.enums.CampaignStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
@org.springframework.test.context.ActiveProfiles("test")
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CampaignService campaignService;

    @Test
    @DisplayName("POST /api/v1/campaigns - Should create promotional campaign")
    void shouldCreateCampaign() throws Exception {
        CampaignCreateRequest request = CampaignCreateRequest.builder()
                .campaignCode("SPRING2026")
                .title("Spring Flash Sale")
                .discountPercentage(BigDecimal.valueOf(25.00))
                .startDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .status(CampaignStatus.SCHEDULED)
                .build();

        CampaignResponse response = CampaignResponse.builder()
                .id(1L)
                .campaignCode("SPRING2026")
                .title("Spring Flash Sale")
                .discountPercentage(BigDecimal.valueOf(25.00))
                .status(CampaignStatus.SCHEDULED)
                .active(false)
                .build();

        when(campaignService.createCampaign(any(CampaignCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.campaignCode").value("SPRING2026"))
                .andExpect(jsonPath("$.data.discountPercentage").value(25.0));
    }

    @Test
    @DisplayName("GET /api/v1/campaigns/active - Should return active campaigns")
    void shouldGetActiveCampaigns() throws Exception {
        CampaignResponse response = CampaignResponse.builder()
                .id(1L)
                .campaignCode("SPRING2026")
                .title("Spring Flash Sale")
                .discountPercentage(BigDecimal.valueOf(25.00))
                .status(CampaignStatus.ACTIVE)
                .active(true)
                .build();

        when(campaignService.getActiveCampaigns()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/campaigns/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].campaignCode").value("SPRING2026"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/campaigns/{code}/status - Should update campaign status")
    void shouldUpdateCampaignStatus() throws Exception {
        CampaignStatusUpdateRequest request = new CampaignStatusUpdateRequest(CampaignStatus.PAUSED);
        CampaignResponse response = CampaignResponse.builder()
                .id(1L)
                .campaignCode("SPRING2026")
                .title("Spring Flash Sale")
                .discountPercentage(BigDecimal.valueOf(25.00))
                .status(CampaignStatus.PAUSED)
                .active(false)
                .build();

        when(campaignService.updateCampaignStatus(eq("SPRING2026"), any(CampaignStatusUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/campaigns/SPRING2026/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PAUSED"));
    }
}
