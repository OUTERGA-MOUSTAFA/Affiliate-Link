package com.affiliateLink.offer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.transaction.support.TransactionTemplate;

import com.affiliateLink.PostgresTestConfiguration;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.profile.AdvertiserInfo;
import com.affiliateLink.profile.ProfileSuspended;

@ApplicationModuleTest
@Import(PostgresTestConfiguration.class)
class OfferEventRegistryIT extends OfferModuleSupportTest {

    private static final Duration WAIT = Duration.ofSeconds(10);

    @Autowired TransactionTemplate transactionTemplate;
    @Autowired ApplicationEventPublisher eventPublisher;

    @Test
    void theEventIsRecordedOncePerListenerAndMarkedCompletedOnceHandled(Scenario scenario) {
        // Given
        saveOffer(7L, CampaignStatus.ACTIVE);

        // When / Then : deux listeners écoutent ProfileSuspended
        scenario.publish(new ProfileSuspended(7L))
                .andWaitAtMost(WAIT)
                .andWaitForStateChange(() -> publications(true), done -> done == 2)
                .andVerify(done -> {
                    assertThat(publications(false)).isZero();
                    Long distinctListeners = jdbcTemplate.queryForObject(
                            "SELECT COUNT(DISTINCT listener_id) FROM event_publication", Long.class);
                    assertThat(distinctListeners).isEqualTo(2);
                });
    }

    @Test
    void aFailingListenerKeepsItsPublicationIncompleteWithoutBlockingTheOthers(Scenario scenario) {
        // Given : l'envoi de notification plante
        saveOffer(7L, CampaignStatus.ACTIVE);
        when(profileApi.getAdvertiser(7L)).thenReturn(Optional.of(
                new AdvertiserInfo(7L, "contact@acme.com", "Acme", "Tech", "https://acme.example.com", false)));
        when(profileApi.getActivePublisherEmails()).thenReturn(List.of("p1@example.com"));
        doThrow(new IllegalStateException("smtp down")).when(notificationSender).send(any(), any(), any());

        // When / Then : le listener des campagnes termine, celui des notifications reste en attente
        scenario.publish(new ProfileSuspended(7L))
                .andWaitAtMost(WAIT)
                .andWaitForStateChange(() -> publications(true), done -> done == 1)
                .andVerify(done -> {
                    verify(notificationSender, timeout(5_000)).send(any(), any(), any());   // il a bien été appelé et a échoué
                    assertThat(campaigns(7L, CampaignStatus.ACTIVE)).isZero();             // les campagnes sont quand même en pause
                    assertThat(publications(false)).isEqualTo(1);                          // la publication n'est pas perdue
                });
    }

    @Test
    void anEventPublishedInARolledBackTransactionIsNeverRecordedNorDelivered() {
        // Given
        saveOffer(7L, CampaignStatus.ACTIVE);

        // When : le module profile annule sa transaction
        transactionTemplate.executeWithoutResult(tx -> {
            eventPublisher.publishEvent(new ProfileSuspended(7L));
            tx.setRollbackOnly();
        });

        // Then : rien ne se passe, même après un temps d'attente
        await().during(Duration.ofMillis(500)).atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            assertThat(publications(true) + publications(false)).isZero();
            assertThat(campaigns(7L, CampaignStatus.ACTIVE)).isEqualTo(1);
        });
        verifyNoInteractions(notificationSender);
    }
}