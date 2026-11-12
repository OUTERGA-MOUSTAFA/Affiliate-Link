package com.affiliateLink.offer.internal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.affiliateLink.common.InvalidStatusTransitionException;

class CampaignEndDateTest {

    private final LocalDate today = LocalDate.now();

    // startDate ديما قبل endDate، باش الـ constructor يقبلها حتى ملي endDate فات
    // private Campaign campaignEndingOn(LocalDate endDate) {
    // return new Campaign("Promo", today.minusDays(10), endDate,
    // BigDecimal.valueOf(500), PayoutModel.CPC, BigDecimal.valueOf(2));
    // }

    private Campaign campaignEndingOn(LocalDate endDate) {
        return new Campaign("Promo", endDate.minusDays(10), endDate,
                BigDecimal.valueOf(500), PayoutModel.CPC, BigDecimal.valueOf(2));
    }

    @ParameterizedTest(name = "end date {0} day(s) from today is refused")
    @ValueSource(longs = { -1, -30 })
    void campaignWithPastEndDateCannotBeActivated(long daysFromToday) {
        // Given
        Campaign campaign = campaignEndingOn(today.plusDays(daysFromToday));

        // When / Then
        assertThatThrownBy(campaign::activate)
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("end date");
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.DRAFT);
    }

    @ParameterizedTest(name = "end date {0} day(s) from today is accepted")
    @ValueSource(longs = { 0, 1, 30 })
    void campaignEndingTodayOrLaterCanBeActivated(long daysFromToday) {
        // Given
        Campaign campaign = campaignEndingOn(today.plusDays(daysFromToday));

        // When
        campaign.activate();

        // Then
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void pausedCampaignWhoseEndDatePassedCannotBeReactivated() {
        // Given: حملة نشيطة تحبست، ومن بعد فات تاريخ النهاية ديالها
        Campaign campaign = campaignEndingOn(today.plusDays(5));
        campaign.activate();
        campaign.pause();
        ReflectionTestUtils.setField(campaign, "endDate", today.minusDays(1));

        // When / Then
        assertThatThrownBy(campaign::activate)
                .isInstanceOf(InvalidStatusTransitionException.class);
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.PAUSED);
    }
}