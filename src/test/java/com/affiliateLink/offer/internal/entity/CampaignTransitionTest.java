package com.affiliateLink.offer.internal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.affiliateLink.common.InvalidStatusTransitionException;

class CampaignTransitionTest {

    // ---------- helpers ----------

    // كيوصل الحملة للحالة المطلوبة غير بالدوال العمومية
    private Campaign campaignIn(CampaignStatus status) {
        Campaign campaign = new Campaign("Summer", LocalDate.now(), LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), PayoutModel.CPC, BigDecimal.valueOf(2));
        switch (status) {
            case DRAFT -> { }
            case ACTIVE -> campaign.activate();
            case PAUSED -> {
                campaign.activate();
                campaign.pause();
            }
            case ENDED -> {
                campaign.activate();
                campaign.end();
            }
        }
        return campaign;
    }

    private void moveTo(Campaign campaign, CampaignStatus target) {
        switch (target) {
            case ACTIVE -> campaign.activate();
            case PAUSED -> campaign.pause();
            case ENDED -> campaign.end();
            default -> throw new IllegalArgumentException("No method leads to " + target);
        }
    }

    // ---------- tests ----------

    @Test
    void newCampaignStartsAsDraft() {
        // When
        Campaign campaign = campaignIn(CampaignStatus.DRAFT);

        // Then
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.DRAFT);
    }

    @ParameterizedTest(name = "{0} -> {1} is allowed")
    @CsvSource({
            "DRAFT,ACTIVE",
            "ACTIVE,PAUSED",
            "ACTIVE,ENDED",
            "PAUSED,ACTIVE",
            "PAUSED,ENDED"
    })
    void allowedTransitionChangesTheStatus(CampaignStatus from, CampaignStatus to) {
        // Given
        Campaign campaign = campaignIn(from);

        // When
        moveTo(campaign, to);

        // Then
        assertThat(campaign.getStatus()).isEqualTo(to);
    }

    @ParameterizedTest(name = "{0} -> {1} is refused")
    @CsvSource({
            "DRAFT,PAUSED",
            "DRAFT,ENDED",
            "ACTIVE,ACTIVE",
            "PAUSED,PAUSED",
            "ENDED,ACTIVE",
            "ENDED,PAUSED",
            "ENDED,ENDED"
    })
    void forbiddenTransitionIsRefusedAndKeepsTheStatus(CampaignStatus from, CampaignStatus to) {
        // Given
        Campaign campaign = campaignIn(from);

        // When / Then
        assertThatThrownBy(() -> moveTo(campaign, to))
                .isInstanceOf(InvalidStatusTransitionException.class);
        assertThat(campaign.getStatus()).isEqualTo(from);
    }

    @Test
    void refusedTransitionMessageNamesBothStatuses() {
        // Given
        Campaign campaign = campaignIn(CampaignStatus.DRAFT);

        // When / Then
        assertThatThrownBy(campaign::pause)
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("PAUSED");
    }

    @Test
    void campaignCanGoThroughItsWholeLifecycle() {
        // Given
        Campaign campaign = campaignIn(CampaignStatus.DRAFT);

        // When
        campaign.activate();
        campaign.pause();
        campaign.activate();
        campaign.end();

        // Then
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.ENDED);
    }
}