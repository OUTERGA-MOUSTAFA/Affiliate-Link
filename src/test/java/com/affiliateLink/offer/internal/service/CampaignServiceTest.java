package com.affiliateLink.offer.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.affiliateLink.common.ConflictException;
import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.Niche;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.internal.dto.CampaignCreateRequest;
import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.PayoutModel;
import com.affiliateLink.offer.internal.mapper.CampaignMapper;
import com.affiliateLink.offer.internal.repository.CampaignRepository;
import com.affiliateLink.offer.internal.repository.OfferRepository;
import com.affiliateLink.offer.internal.service.impliments.CampaignServiceImpl;
import com.affiliateLink.profile.ProfileApi;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    // ---- الـ "ممثلين": نسخ مزيفة بلا database ----
    @Mock
    CampaignRepository campaignRepository;
    @Mock
    OfferRepository offerRepository;
    @Mock
    CampaignMapper campaignMapper;
    @Mock
    ProfileApi profileApi;

    // ---- الـ service الحقيقي، كيتحقن فيه الممثلين ----
    @InjectMocks
    CampaignServiceImpl campaignService;
    private final LocalDate today = LocalDate.now();

    private final CampaignCreateRequest request = new CampaignCreateRequest(
            "Summer", today, today.plusDays(10), BigDecimal.valueOf(1000),
            PayoutModel.CPC, BigDecimal.valueOf(2));

    // ---- helpers ----

    private Offer offer() {
        return new Offer(7L, "Pro VPN", "d", "https://vpn.example.com", Niche.TECH);
    }

    private Campaign campaignOf(Offer offer, long id) {
        Campaign c = new Campaign("C" + id, today, today.plusDays(10),
                BigDecimal.valueOf(100), PayoutModel.CPC, BigDecimal.valueOf(2));
        offer.addCampaign(c);
        ReflectionTestUtils.setField(c, "id", id); // فـ الـ test ما كاينش database تعطي id
        return c;
    }

    // ---- create() ----

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
        assertEquals(1, offer.getCampaigns().size());
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

    // ---- changeStatus() : التفعيل الحصري ----

    @Test
    void activatingACampaignPausesThePreviousActiveOne() {
        Offer offer = offer();
        ReflectionTestUtils.setField(offer, "id", 1L);

        Campaign old = campaignOf(offer, 1L);
        old.activate(); // القديمة ACTIVE
        Campaign next = campaignOf(offer, 2L); // الجديدة DRAFT

        when(campaignRepository.findById(2L)).thenReturn(Optional.of(next));
        when(campaignRepository.findByOfferIdAndStatus(1L, CampaignStatus.ACTIVE)).thenReturn(Optional.of(old));

        campaignService.changeStatus(2L, CampaignStatus.ACTIVE);

        assertEquals(CampaignStatus.PAUSED, old.getStatus());
        assertEquals(CampaignStatus.ACTIVE, next.getStatus());
        verify(campaignRepository).saveAndFlush(old); // كتكتب قبل
    }

    @Test
    void reactivatingAnAlreadyActiveCampaignIsRefusedAndPausesNothing() {
        Offer offer = offer();
        ReflectionTestUtils.setField(offer, "id", 1L);

        Campaign active = campaignOf(offer, 1L);
        active.activate();

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(active));
        when(campaignRepository.findByOfferIdAndStatus(1L, CampaignStatus.ACTIVE)).thenReturn(Optional.of(active));

        assertThrows(InvalidStatusTransitionException.class,
                () -> campaignService.changeStatus(1L, CampaignStatus.ACTIVE));

        verify(campaignRepository, never()).saveAndFlush(any());
    }
}