package com.affiliateLink.offer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import com.affiliateLink.PostgresTestConfiguration;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.profile.AdvertiserInfo;
import com.affiliateLink.profile.ProfileSuspended;

@ApplicationModuleTest
@Import(PostgresTestConfiguration.class)
class OfferModuleTest extends OfferModuleSupportTest {

    private static final Duration WAIT = Duration.ofSeconds(10);

    @Test
    void suspendingAnAdvertiserPausesAllItsActiveCampaigns(Scenario scenario) {
        // Given : deux offres de l'advertiser 7, chacune avec une campagne active
        saveOffer(7L, CampaignStatus.ACTIVE);
        saveOffer(7L, CampaignStatus.ACTIVE);

        // When / Then : on attend que plus aucune campagne ne soit ACTIVE
        scenario.publish(new ProfileSuspended(7L))
                .waitAtMost(WAIT)
                .andWaitForStateChange(() -> campaigns(7L, CampaignStatus.ACTIVE), active -> active == 0)
                .andVerify(active -> assertThat(campaigns(7L, CampaignStatus.PAUSED)).isEqualTo(2));
    }

    @Test
    void suspendingAnAdvertiserOnlyTouchesItsActiveCampaigns(Scenario scenario) {
        // Given
        saveOffer(7L, CampaignStatus.ACTIVE, CampaignStatus.DRAFT, CampaignStatus.ENDED);
        saveOffer(8L, CampaignStatus.ACTIVE);

        // When / Then : on attend que les deux listeners aient terminé
        scenario.publish(new ProfileSuspended(7L))
                .waitAtMost(WAIT)
                .andWaitForStateChange(() -> publications(true), done -> done == 2)
                .andVerify(done -> {
                    assertThat(campaigns(7L, CampaignStatus.PAUSED)).isEqualTo(1);
                    assertThat(campaigns(7L, CampaignStatus.DRAFT)).isEqualTo(1);
                    assertThat(campaigns(7L, CampaignStatus.ENDED)).isEqualTo(1);
                    assertThat(campaigns(8L, CampaignStatus.ACTIVE)).isEqualTo(1);   // autre advertiser : intact
                });
    }

    @Test
    void suspendingAnAdvertiserWithoutActiveCampaignChangesNothing(Scenario scenario) {
        // Given
        saveOffer(7L, CampaignStatus.DRAFT, CampaignStatus.PAUSED);

        // When / Then
        scenario.publish(new ProfileSuspended(7L))
                .waitAtMost(WAIT)
                .andWaitForStateChange(() -> publications(true), done -> done == 2)
                .andVerify(done -> {
                    assertThat(campaigns(7L, CampaignStatus.DRAFT)).isEqualTo(1);
                    assertThat(campaigns(7L, CampaignStatus.PAUSED)).isEqualTo(1);
                });
    }

    @Test
    void suspendingAnAdvertiserNotifiesTheActivePublishers(Scenario scenario) {
        // Given
        List<String> emails = List.of("p1@example.com", "p2@example.com");
        when(profileApi.getAdvertiser(7L)).thenReturn(Optional.of(
                new AdvertiserInfo(7L, "contact@acme.com", "Acme", "Tech", "https://acme.example.com", false)));
        when(profileApi.getActivePublisherEmails()).thenReturn(emails);

        // When / Then
        scenario.publish(new ProfileSuspended(7L))
                .waitAtMost(WAIT)
                .andWaitForStateChange(() -> publications(true), done -> done == 2)
                .andVerify(done -> verify(notificationSender).send(eq(emails), anyString(), anyString()));
    }

    @Test
    void suspendingAPublisherNotifiesNobody(Scenario scenario) {
        // Given : ProfileApi ne connaît aucun advertiser 9 (Optional vide par défaut)

        // When / Then
        scenario.publish(new ProfileSuspended(9L))
                .waitAtMost(WAIT)
                .andWaitForStateChange(() -> publications(true), done -> done == 2)
                .andVerify(done -> verifyNoInteractions(notificationSender));
    }
}