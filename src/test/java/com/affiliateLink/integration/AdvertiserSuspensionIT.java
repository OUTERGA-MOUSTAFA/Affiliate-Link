package com.affiliateLink.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;

@ExtendWith(OutputCaptureExtension.class)
class AdvertiserSuspensionIT extends AbstractIntegrationTest {

    private static final Duration WAIT = Duration.ofSeconds(10);

    // les listeners sont asynchrones : on attend l'effet, sans Thread.sleep
    private void awaitActiveCampaigns(int expected) {
        await().atMost(WAIT).untilAsserted(() -> mockMvc
                .perform(get("/api/campaigns").param("status", "ACTIVE"))
                .andExpect(jsonPath("$.length()").value(expected)));
    }

    private void awaitHandledEvents(long expected) {
        await().atMost(WAIT).untilAsserted(() -> assertThat(
                count("SELECT COUNT(*) FROM event_publication WHERE completion_date IS NOT NULL"))
                .isEqualTo(expected));
    }

    // une offre avec une campagne active
    private long activeCampaignOf(long advertiser, String title) throws Exception {
        long offer = createOffer(advertiser, title, "TECH");
        long campaign = createCampaign(offer, "Campaign of " + title);
        changeCampaignStatus(campaign, "ACTIVE");
        return campaign;
    }

    @Test
    void suspendingAnAdvertiserPausesAllItsActiveCampaignsAndOnlyThose() throws Exception {
        // Given : Acme a deux campagnes actives et une en DRAFT ; North une campagne active
        long acme = activeAdvertiser("Acme");
        long north = activeAdvertiser("North");
        activeCampaignOf(acme, "Acme 1");
        activeCampaignOf(acme, "Acme 2");
        long draftOffer = createOffer(acme, "Acme 3", "TECH");
        createCampaign(draftOffer, "Draft campaign");
        long northCampaign = activeCampaignOf(north, "North 1");

        // When
        setProfileStatus(acme, "SUSPENDED");

        // Then : il ne reste que la campagne de North
        awaitActiveCampaigns(1);
        mockMvc.perform(get("/api/campaigns").param("status", "ACTIVE"))
                .andExpect(jsonPath("$[0].id").value(northCampaign));
        mockMvc.perform(get("/api/campaigns").param("status", "PAUSED"))
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/campaigns").param("status", "DRAFT"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void bothListenersCompleteAndTheRegistryKeepsNothingPending() throws Exception {
        long acme = activeAdvertiser("Acme");
        activeCampaignOf(acme, "Acme 1");

        setProfileStatus(acme, "SUSPENDED");

        awaitHandledEvents(2);   // un listener pour les campagnes, un pour les notifications
        assertThat(count("SELECT COUNT(*) FROM event_publication WHERE completion_date IS NULL")).isZero();
    }

    @Test
    void aSuspendedAdvertiserCannotPublishAnymore() throws Exception {
        // Given
        long acme = activeAdvertiser("Acme");
        long offer = createOffer(acme, "Pro VPN", "TECH");
        setProfileStatus(acme, "SUSPENDED");
        awaitHandledEvents(2);

        // Then : création d'offre, de campagne et modification d'offre sont refusées (409)
        postOffer(acme, "Another offer", "TECH").andExpect(status().isConflict());
        postCampaign(offer, "Late campaign", LocalDate.now(), LocalDate.now().plusDays(30), "CPC", "0.50")
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/offres/{id}", offer).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"New title","description":"d","landingUrl":"https://example.com","niche":"TECH","tags":[]}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void reactivatingAnAdvertiserDoesNotReactivateItsCampaigns() throws Exception {
        // Given
        long acme = activeAdvertiser("Acme");
        activeCampaignOf(acme, "Acme 1");
        setProfileStatus(acme, "SUSPENDED");
        awaitActiveCampaigns(0);
        awaitHandledEvents(2);

        // When
        setProfileStatus(acme, "ACTIVE");

        // Then : l'activation ne publie aucun événement, les campagnes restent en pause
        mockMvc.perform(get("/api/campaigns").param("status", "ACTIVE"))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/campaigns").param("status", "PAUSED"))
                .andExpect(jsonPath("$.length()").value(1));
        assertThat(count("SELECT COUNT(*) FROM event_publication")).isEqualTo(2);
    }

    @Test
    void suspendingAPublisherLeavesEveryCampaignUntouched() throws Exception {
        // Given
        long acme = activeAdvertiser("Acme");
        activeCampaignOf(acme, "Acme 1");
        long publisher = activePublisher();

        // When
        setProfileStatus(publisher, "SUSPENDED");

        // Then : l'événement est traité, mais aucun advertiser n'est concerné
        awaitHandledEvents(2);
        mockMvc.perform(get("/api/campaigns").param("status", "ACTIVE"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void suspendingAnAdvertiserNotifiesTheActivePublishers(CapturedOutput output) throws Exception {
        // Given
        long acme = activeAdvertiser("Acme");
        activePublisher();

        // When
        setProfileStatus(acme, "SUSPENDED");

        // Then
        await().atMost(WAIT).untilAsserted(() -> assertThat(output.getAll())
                .contains("Stop sharing links of Acme")
                .contains("sent to 1 publisher(s)"));
    }
}