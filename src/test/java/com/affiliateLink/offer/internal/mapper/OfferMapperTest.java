package com.affiliateLink.offer.internal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.affiliateLink.common.Niche;   // بدل المسار إلا خليتو فـ بلاصة أخرى
import com.affiliateLink.offer.internal.dto.OfferCreateRequest;
import com.affiliateLink.offer.internal.dto.OfferResponse;
import com.affiliateLink.offer.internal.dto.OfferSummaryResponse;
import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.PayoutModel;
import com.affiliateLink.offer.internal.entity.Tag;

// كيشعل غير الـ mappers: بلا Spring Boot كامل وبلا database
@SpringJUnitConfig({OfferMapperImpl.class, CampaignMapperImpl.class})
class OfferMapperTest {

    @Autowired
    private OfferMapper offerMapper;

    private Offer sampleOffer() {
        return Offer.class.descriptorString();
        // Offer offer = new Offer(7L, "Pro VPN", "Fast VPN", "https://vpn.example.com", Niche.TECH);
        // offer.replaceTags(Set.of(new Tag(" VPN ")));
        // offer.add(new Campaign("Summer", LocalDate.now(), LocalDate.now().plusDays(10),
        //         BigDecimal.valueOf(500), PayoutModel.CPC, BigDecimal.valueOf(2)));
        // return offer;
    }

    @Test
    void toEntityCopiesFieldsAndLeavesTagsEmpty() {
        OfferCreateRequest request = new OfferCreateRequest(
                7L, "Pro VPN", "Fast VPN", "https://vpn.example.com", Niche.TECH, Set.of("vpn"));

        Offer offer = offerMapper.toEntity(request);

        assertEquals(7L, offer.getAdvertiserId());
        assertEquals("Pro VPN", offer.getTitle());
        assertEquals(Niche.TECH, offer.getNiche());
        assertNotNull(offer.getCreatedAt());
        assertTrue(offer.getTags().isEmpty());        // كيتعمرو فـ الـ service
        assertTrue(offer.getCampaigns().isEmpty());
    }

    @Test
    void toResponseIncludesTagNamesAndCampaigns() {
        OfferResponse response = offerMapper.toResponse(sampleOffer());

        assertEquals("Pro VPN", response.title());
        assertEquals(Niche.TECH, response.niche());
        assertEquals(Set.of("vpn"), response.tags());              // trim + lowercase
        assertEquals(1, response.campaigns().size());
        assertEquals("Summer", response.campaigns().get(0).name());
        assertEquals(CampaignStatus.DRAFT, response.campaigns().get(0).status());
    }

    @Test
    void toSummaryHasTagsButNoCampaigns() {
        OfferSummaryResponse summary = offerMapper.toSummary(sampleOffer());

        assertEquals("Pro VPN", summary.title());
        assertEquals(Set.of("vpn"), summary.tags());
    }
}