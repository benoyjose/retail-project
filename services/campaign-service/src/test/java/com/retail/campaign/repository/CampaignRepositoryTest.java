package com.retail.campaign.repository;

import com.retail.campaign.entity.Campaign;
import com.retail.common.enums.CampaignStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CampaignRepositoryTest {

    @Autowired
    private CampaignRepository campaignRepository;

    private Campaign testCampaign;

    @BeforeEach
    void setUp() {
        campaignRepository.deleteAll();

        testCampaign = Campaign.builder()
                .campaignCode("SUMMER2026")
                .title("Summer Sale")
                .description("Seasonal discount on summer electronics")
                .discountPercentage(BigDecimal.valueOf(15.00))
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .status(CampaignStatus.ACTIVE)
                .applicableCategoryIds(List.of(1L, 2L))
                .applicableSkus(List.of("SKU-SUMMER-01"))
                .build();
    }

    @Test
    @DisplayName("Should save and find campaign by campaignCode")
    void shouldSaveAndFindByCampaignCode() {
        Campaign saved = campaignRepository.save(testCampaign);
        assertThat(saved.getId()).isNotNull();

        Optional<Campaign> found = campaignRepository.findByCampaignCode("SUMMER2026");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Summer Sale");
        assertThat(found.get().getDiscountPercentage()).isEqualByComparingTo(BigDecimal.valueOf(15.00));
        assertThat(found.get().getApplicableSkus()).contains("SKU-SUMMER-01");
    }

    @Test
    @DisplayName("Should find active campaigns within time window")
    void shouldFindActiveCampaignsWithinTimeWindow() {
        campaignRepository.save(testCampaign);

        Campaign expiredCampaign = Campaign.builder()
                .campaignCode("EXPIRED2025")
                .title("Expired Promo")
                .discountPercentage(BigDecimal.valueOf(10.00))
                .startDate(Instant.now().minus(10, ChronoUnit.DAYS))
                .endDate(Instant.now().minus(2, ChronoUnit.DAYS))
                .status(CampaignStatus.ACTIVE)
                .build();
        campaignRepository.save(expiredCampaign);

        List<Campaign> active = campaignRepository.findActiveCampaignsAt(Instant.now());
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getCampaignCode()).isEqualTo("SUMMER2026");
    }

    @Test
    @DisplayName("Should check if campaign exists by code")
    void shouldCheckIfExistsByCode() {
        campaignRepository.save(testCampaign);

        assertThat(campaignRepository.existsByCampaignCode("SUMMER2026")).isTrue();
        assertThat(campaignRepository.existsByCampaignCode("UNKNOWN")).isFalse();
    }
}

