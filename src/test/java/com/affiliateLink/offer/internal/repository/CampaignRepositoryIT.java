package com.affiliateLink.offer.internal.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.affiliateLink.PostgresTestConfiguration;
import com.affiliateLink.common.Niche;
import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.PayoutModel;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestConfiguration.class)
class CampaignRepositoryIT {

    @Autowired CampaignRepository campaignRepository;
    @Autowired OfferRepository offerRepository;
    @Autowired EntityManager em;

    // ---------- findByStatus ----------

    @Test
    void findByStatusReturnsOnlyCampaignsInThatStatus() {
        // Given
        offerWith(7L, CampaignStatus.ACTIVE, CampaignStatus.DRAFT);
        offerWith(8L, CampaignStatus.ACTIVE, CampaignStatus.PAUSED, CampaignStatus.ENDED);
        reload();

        // When
        List<Campaign> active = campaignRepository.findByStatus(CampaignStatus.ACTIVE);

        // Then
        assertThat(active).extracting(c -> c.getOffer().getAdvertiserId())
                .containsExactlyInAnyOrder(7L, 8L);
    }

    @Test
    void findByStatusWithNoMatchReturnsAnEmptyList() {
        offerWith(7L, CampaignStatus.DRAFT);
        reload();

        assertThat(campaignRepository.findByStatus(CampaignStatus.ENDED)).isEmpty();
    }

    // ---------- findByOfferIdAndStatus ----------

    @Test
    void findByOfferIdAndStatusReturnsTheActiveCampaignOfThatOffer() {
        Offer offer = offerWith(7L, CampaignStatus.ACTIVE, CampaignStatus.DRAFT);
        offerWith(8L, CampaignStatus.ACTIVE);
        reload();

        Optional<Campaign> found = campaignRepository.findByOfferIdAndStatus(offer.getId(), CampaignStatus.ACTIVE);

        assertThat(found).isPresent();
        assertThat(found.get().getOffer().getId()).isEqualTo(offer.getId());
    }

    @Test
    void findByOfferIdAndStatusIsEmptyWhenTheOfferHasNoSuchCampaign() {
        Offer offer = offerWith(7L, CampaignStatus.DRAFT, CampaignStatus.PAUSED);
        reload();

        assertThat(campaignRepository.findByOfferIdAndStatus(offer.getId(), CampaignStatus.ACTIVE)).isEmpty();
    }

    // ---------- findByOfferAdvertiserIdAndStatus (utilisé par le listener) ----------

    @Test
    void findByAdvertiserAndStatusSpansAllOffersOfThatAdvertiser() {
        offerWith(7L, CampaignStatus.ACTIVE);
        offerWith(7L, CampaignStatus.ACTIVE);
        offerWith(8L, CampaignStatus.ACTIVE);
        reload();

        assertThat(campaignRepository.findByOfferAdvertiserIdAndStatus(7L, CampaignStatus.ACTIVE)).hasSize(2);
        assertThat(campaignRepository.findByOfferAdvertiserIdAndStatus(8L, CampaignStatus.ACTIVE)).hasSize(1);
    }

    @Test
    void findByAdvertiserAndStatusIgnoresOtherStatusesAndOtherAdvertisers() {
        offerWith(7L, CampaignStatus.PAUSED, CampaignStatus.ENDED);
        reload();

        assertThat(campaignRepository.findByOfferAdvertiserIdAndStatus(7L, CampaignStatus.ACTIVE)).isEmpty();
        assertThat(campaignRepository.findByOfferAdvertiserIdAndStatus(99L, CampaignStatus.PAUSED)).isEmpty();
    }

    // ---------- index unique partiel : une seule campagne ACTIVE par offre ----------

    @Test
    void twoActiveCampaignsOnTheSameOfferAreRejectedByTheDatabase() {
        assertThatThrownBy(() -> offerWith(7L, CampaignStatus.ACTIVE, CampaignStatus.ACTIVE))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void anOfferMayHaveSeveralCampaignsThatAreNotActive() {
        assertThatCode(() -> offerWith(7L, CampaignStatus.DRAFT, CampaignStatus.PAUSED, CampaignStatus.ENDED,
                CampaignStatus.ACTIVE)).doesNotThrowAnyException();
    }

    @Test
    void differentOffersMayEachHaveTheirOwnActiveCampaign() {
        assertThatCode(() -> {
            offerWith(7L, CampaignStatus.ACTIVE);
            offerWith(7L, CampaignStatus.ACTIVE);
        }).doesNotThrowAnyException();
    }

    // ---------- helpers ----------

    // crée une offre avec une campagne par statut demandé (on y arrive par les transitions de l'entité)
    private Offer offerWith(long advertiserId, CampaignStatus... statuses) {
        Offer offer = new Offer(advertiserId, "Offer", "d", "https://example.com", Niche.TECH);
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

    // on vide le cache de Hibernate pour relire vraiment depuis PostgreSQL
    private void reload() {
        em.flush();
        em.clear();
    }
}