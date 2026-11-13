package com.affiliateLink.offer.internal.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.entity.PayoutModel;
import com.affiliateLink.offer.internal.repository.CampaignRepository;
import com.affiliateLink.profile.ProfileSuspended;

@ExtendWith(MockitoExtension.class)
class ProfileSuspendedListenerTest {

    @Mock CampaignRepository campaignRepository;
    @InjectMocks ProfileSuspendedListener listener;

    private Campaign activeCampaign() {
        Campaign c = new Campaign("C", LocalDate.now(), LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), PayoutModel.CPC, BigDecimal.valueOf(2));
        c.activate();
        return c;
    }

    @Test
    void pausesAllActiveCampaignsOfTheSuspendedAdvertiser() {
        Campaign first = activeCampaign();
        Campaign second = activeCampaign();
        when(campaignRepository.findByOfferAdvertiserIdAndStatus(7L, CampaignStatus.ACTIVE))
                .thenReturn(List.of(first, second));

        listener.on(new ProfileSuspended(7L));

        assertEquals(CampaignStatus.PAUSED, first.getStatus());
        assertEquals(CampaignStatus.PAUSED, second.getStatus());
    }

    @Test
    void doesNothingWhenAdvertiserHasNoActiveCampaign() {
        // Given
        when(campaignRepository.findByOfferAdvertiserIdAndStatus(7L, CampaignStatus.ACTIVE))
                .thenReturn(List.of());

        // When
        listener.on(new ProfileSuspended(7L));

        // Then : le repository a été interrogé une fois, et rien d'autre (aucun save)
        verify(campaignRepository).findByOfferAdvertiserIdAndStatus(7L, CampaignStatus.ACTIVE);
        verifyNoMoreInteractions(campaignRepository);
    }
}