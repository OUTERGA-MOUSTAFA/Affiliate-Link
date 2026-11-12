package com.affiliateLink.offer;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.affiliateLink.common.Niche;
import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.PayoutModel;
import com.affiliateLink.offer.internal.repository.CampaignRepository;
import com.affiliateLink.offer.internal.repository.OfferRepository;
import com.affiliateLink.offer.internal.service.NotificationSender;
import com.affiliateLink.profile.ProfileApi;

abstract class OfferModuleSupportTest {

    @Autowired protected OfferRepository offerRepository;
    @Autowired protected CampaignRepository campaignRepository;
    @Autowired protected JdbcTemplate jdbcTemplate;

    // ce que le module offer attend du module profile : on le simule
    @MockitoBean protected ProfileApi profileApi;
    @MockitoBean protected NotificationSender notificationSender;

    // Le Scenario valide ses données : on nettoie à la main
    @AfterEach
    void cleanDatabase() {
        offerRepository.deleteAll();   // les campagnes partent avec leur offre (cascade)
        jdbcTemplate.update("DELETE FROM event_publication");
    }

    // une offre avec une campagne par statut demandé (au plus une ACTIVE par offre)
    protected Offer saveOffer(long advertiserId, CampaignStatus... statuses) {
        Offer offer = new Offer(advertiserId, "Offer of " + advertiserId, "d", "https://example.com", Niche.TECH);
        for (CampaignStatus status : statuses) {
            offer.addCampaign(campaignIn(status));
        }
        return offerRepository.saveAndFlush(offer);
    }

    private Campaign campaignIn(CampaignStatus status) {
        Campaign campaign = new Campaign("C-" + status, LocalDate.now(), LocalDate.now().plusDays(10),
                BigDecimal.valueOf(100), PayoutModel.CPC, BigDecimal.valueOf(2));
        switch (status) {
            case DRAFT -> { }
            case ACTIVE -> campaign.activate();
            case PAUSED -> { campaign.activate(); campaign.pause(); }
            case ENDED -> { campaign.activate(); campaign.end(); }
        }
        return campaign;
    }

    protected long campaigns(long advertiserId, CampaignStatus status) {
        return campaignRepository.findByOfferAdvertiserIdAndStatus(advertiserId, status).size();
    }

    // nombre de publications de ProfileSuspended dans le registre (terminées ou non)
    protected long publications(boolean completed) {
        String sql = "SELECT COUNT(*) FROM event_publication WHERE event_type LIKE '%ProfileSuspended' "
                + "AND completion_date IS " + (completed ? "NOT NULL" : "NULL");
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}