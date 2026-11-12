package com.affiliateLink.integration;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CampaignJourneyIT extends AbstractIntegrationTest {

    // ---------- l'advertiser doit être actif ----------

    @Test
    void aPendingAdvertiserCannotPublishAnOffer() throws Exception {
        long pending = createAdvertiser(uniqueEmail(), "Acme");

        postOffer(pending, "Pro VPN", "TECH").andExpect(status().isConflict());

        Assertions.assertThat(count("SELECT COUNT(*) FROM offer_offers")).isZero();
    }

    @Test
    void anOfferForAnUnknownAdvertiserReturns404() throws Exception {
        postOffer(999_999L, "Pro VPN", "TECH").andExpect(status().isNotFound());

        Assertions.assertThat(count("SELECT COUNT(*) FROM offer_offers")).isZero();
    }

    // ---------- offre et tags ----------

    @Test
    void tagsAreStoredInLowercaseWithoutDuplicatesAndSharedBetweenOffers() throws Exception {
        long advertiser = activeAdvertiser("Acme");

        postOffer(advertiser, "Pro VPN", "TECH", "VPN", "Security", " vpn ")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags", containsInAnyOrder("vpn", "security")));
        postOffer(advertiser, "Cloud Backup", "TECH", "vpn", "Cloud")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags", containsInAnyOrder("vpn", "cloud")));

        List<String> names = jdbcTemplate.queryForList("SELECT name FROM offer_tags ORDER BY name", String.class);
        Assertions.assertThat(names).containsExactly("cloud", "security", "vpn");   // "vpn" n'existe qu'une fois
    }

    // ---------- campagnes ----------

    @Test
    void aNewCampaignStartsAsDraft() throws Exception {
        long offer = createOffer(activeAdvertiser("Acme"), "Pro VPN", "TECH");

        postCampaign(offer, "Winter", LocalDate.now(), LocalDate.now().plusDays(30), "CPC", "0.50")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.offerId").value(offer));
    }

    @Test
    void aCampaignForAnUnknownOfferReturns404() throws Exception {
        postCampaign(999_999L, "Winter", LocalDate.now(), LocalDate.now().plusDays(30), "CPC", "0.50")
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidCampaignsAreRefusedAndNothingIsStored() throws Exception {
        long offer = createOffer(activeAdvertiser("Acme"), "Pro VPN", "TECH");
        LocalDate today = LocalDate.now();

        postCampaign(offer, "Bad dates", today, today.minusDays(1), "CPC", "0.50")
                .andExpect(status().isBadRequest());
        postCampaign(offer, "Percentage above 100", today, today.plusDays(30), "CPA_PERCENT", "150")
                .andExpect(status().isBadRequest());
        postCampaign(offer, "Zero payout", today, today.plusDays(30), "CPL", "0")
                .andExpect(status().isBadRequest());

        Assertions.assertThat(count("SELECT COUNT(*) FROM offer_campaigns")).isZero();
    }

    // ---------- activations successives ----------

    @Test
    void activatingASecondCampaignPausesTheFirstOne() throws Exception {
        // Given : une offre et deux campagnes
        long offer = createOffer(activeAdvertiser("Acme"), "Pro VPN", "TECH");
        long first = createCampaign(offer, "First");
        long second = createCampaign(offer, "Second");

        // When : on active la première
        changeCampaignStatus(first, "ACTIVE");

        // Then
        mockMvc.perform(get("/api/campaigns").param("status", "ACTIVE"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(first));

        // When : on active la seconde
        changeCampaignStatus(second, "ACTIVE");

        // Then : la seconde est active, la première est passée en PAUSED
        mockMvc.perform(get("/api/campaigns").param("status", "ACTIVE"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(second));
        mockMvc.perform(get("/api/campaigns").param("status", "PAUSED"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(first));
    }

    @Test
    void anOfferIsReturnedWithItsCampaignsAndTheirStatuses() throws Exception {
        long offer = createOffer(activeAdvertiser("Acme"), "Pro VPN", "TECH");
        long first = createCampaign(offer, "First");
        long second = createCampaign(offer, "Second");
        changeCampaignStatus(first, "ACTIVE");
        changeCampaignStatus(second, "ACTIVE");

        mockMvc.perform(get("/api/offres/{id}", offer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns.length()").value(2))
                .andExpect(jsonPath("$.campaigns[?(@.name=='First')].status", contains("PAUSED")))
                .andExpect(jsonPath("$.campaigns[?(@.name=='Second')].status", contains("ACTIVE")));
    }

    @Test
    void aCampaignGoesThroughItsWholeLifecycle() throws Exception {
        long offer = createOffer(activeAdvertiser("Acme"), "Pro VPN", "TECH");
        long campaign = createCampaign(offer, "Winter");

        changeCampaignStatus(campaign, "ACTIVE");
        changeCampaignStatus(campaign, "PAUSED");
        changeCampaignStatus(campaign, "ACTIVE");
        changeCampaignStatus(campaign, "ENDED");

        mockMvc.perform(get("/api/campaigns").param("status", "ENDED"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void anEndedCampaignCannotBeReactivated() throws Exception {
        long offer = createOffer(activeAdvertiser("Acme"), "Pro VPN", "TECH");
        long campaign = createCampaign(offer, "Winter");
        changeCampaignStatus(campaign, "ACTIVE");
        changeCampaignStatus(campaign, "ENDED");

        patchCampaignStatus(campaign, "ACTIVE").andExpect(status().isConflict());

        mockMvc.perform(get("/api/campaigns").param("status", "ENDED"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @ParameterizedTest(name = "DRAFT -> {0} is refused")
    @ValueSource(strings = { "PAUSED", "ENDED", "DRAFT" })
    void aDraftCampaignCanOnlyBeActivated(String target) throws Exception {
        long offer = createOffer(activeAdvertiser("Acme"), "Pro VPN", "TECH");
        long campaign = createCampaign(offer, "Winter");

        patchCampaignStatus(campaign, target).andExpect(status().isConflict());

        mockMvc.perform(get("/api/offres/{id}", offer))
                .andExpect(jsonPath("$.campaigns[0].status").value("DRAFT"));
    }

    @Test
    void aCampaignWhoseEndDatePassedCannotBeActivatedAndKeepsItsStatus() throws Exception {
        // Given : dates valides à la création (fin > début), mais déjà dans le passé
        long offer = createOffer(activeAdvertiser("Acme"), "Pro VPN", "TECH");
        long expired = idOf(postCampaign(offer, "Expired", LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1), "CPC", "0.50").andExpect(status().isCreated()));

        // When / Then
        patchCampaignStatus(expired, "ACTIVE").andExpect(status().isConflict());
        mockMvc.perform(get("/api/offres/{id}", offer))
                .andExpect(jsonPath("$.campaigns[0].status").value("DRAFT"));
    }

    @Test
    void aFailedActivationLeavesThePreviouslyActiveCampaignActive() throws Exception {
        // Given : une campagne active et une campagne expirée sur la même offre
        long offer = createOffer(activeAdvertiser("Acme"), "Pro VPN", "TECH");
        long running = createCampaign(offer, "Running");
        changeCampaignStatus(running, "ACTIVE");
        long expired = idOf(postCampaign(offer, "Expired", LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1), "CPC", "0.50").andExpect(status().isCreated()));

        // When : l'activation échoue
        patchCampaignStatus(expired, "ACTIVE").andExpect(status().isConflict());

        // Then : la transaction est annulée, l'ancienne campagne reste active (atomicité)
        mockMvc.perform(get("/api/campaigns").param("status", "ACTIVE"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(running));
    }
}