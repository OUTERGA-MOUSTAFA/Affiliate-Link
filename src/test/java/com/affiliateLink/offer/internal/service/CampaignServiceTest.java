package com.affiliateLink.offer.internal.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.affiliateLink.offer.internal.dto.CampaignCreateRequest;
import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.mapper.CampaignMapper;
import com.affiliateLink.offer.internal.repository.CampaignRepository;
import com.affiliateLink.offer.internal.repository.OfferRepository;
import com.affiliateLink.profile.ProfileApi;

@ExtendWith (MockitoExtension.class)
class CampaignServiceTest {

    @Mock 
    CampaignRepository campaignRepository;
    @Mock
    OfferRepository offerRepository;
    @Mock
    CampaignMapper campaignMapper;
    @Mock
    ProfileApi  profileApi;
    @InjectMocks
    CampaignService campaignService;

    private final LocalDate today = LocalDate.now();
    private final CampaignCreateRequest request = new CampaignCreateRequest(
            "Summer", today, today.plusDays(10), BigDecimal.valueOf(1000),
            PayoutModel.CPC, BigDecimal.valueOf(2));

    private Offer offer() {
        return new Offer(7L, "Pro VPN", "d", "https://vpn.example.com", Niche.TECH);
    }

    @Test 
    void createsDraftCampaignForActiveAdvertiser() {
        Offer offer = offer();
        Campaign campaign = new Campaign("Summer", today, today.plusDays(10),
                BigDecimal.valueOf(1000), PayoutModel.CPC, BigDecimal.valueOf(2));

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(profileApi.isActive(7L)).thenReturn(true);
        when(campaignMapper.toEntity(request)).thenReturn(campaign);
        when(campaignRepository.save(campaign)).thenReturn(campaign);

        campaignService.create(1L, request);

        assertEquals(CampaignStatus.DRAFT, campaign.getStatus());
        assertEquals(1, offer.getCampaigns().size()); // كتربطات بالعرض
        verify(campaignRepository).save(campaign);
    }

    @Test
    void refusesWhenAdvertiserIsNotActive() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer()));
        when(profileApi.isActive(7L)).thenReturn(false);

        assertThrows(ConflictException.class, () -> campaignService.create(1L, request));

        verify(campaignRepository, never()).save(any());
    }

    @Test
    void refusesWhenOfferDoesNotExist() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> campaignService.create(99L, request));

        verify(campaignRepository, never()).save(any());
    }
}