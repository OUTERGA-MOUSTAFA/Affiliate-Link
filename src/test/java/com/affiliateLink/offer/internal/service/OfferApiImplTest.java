package com.affiliateLink.offer.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.affiliateLink.common.Niche;
import com.affiliateLink.offer.CampaignInfo;
import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.PayoutModel;
import com.affiliateLink.offer.internal.repository.CampaignRepository;

@ExtendWith(MockitoExtension.class)
class OfferApiImplTest {

    @Mock
    CampaignRepository campaignRepository;
    @InjectMocks
    OfferApiImpl offerApi;

    @Test
    void returnsEmptyWhenCampaignDoesNotExist() {
        when(campaignRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(offerApi.getCampaign(99L).isEmpty());
    }

    @Test
    void mapsCampaignToInfoWithoutExposingTheEntity() {
        Offer offer = new Offer(7L, "Pro VPN", "d", "https://vpn.example.com", Niche.TECH);
        Campaign campaign = new Campaign("Summer", LocalDate.now(), LocalDate.now().plusDays(10),
                BigDecimal.valueOf(500), PayoutModel.CPC, BigDecimal.valueOf(2));
        offer.addCampaign(campaign);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        CampaignInfo info = offerApi.getCampaign(1L).orElseThrow();

        assertEquals(7L, info.advertiserId());
        assertEquals("CPC", info.payoutModel());
        assertEquals("DRAFT", info.status());
    }
}