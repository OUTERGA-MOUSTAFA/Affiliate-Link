package com.affiliateLink.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.PublishedEvents;
import org.springframework.transaction.annotation.Transactional;

import com.affiliateLink.PostgresTestConfiguration;
import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.Niche;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.internal.entity.Advertiser;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.entity.Publisher;
import com.affiliateLink.profile.internal.repository.AdvertiserRepository;
import com.affiliateLink.profile.internal.repository.PublisherRepository;
import com.affiliateLink.profile.internal.service.AdminProfileService;

@ApplicationModuleTest
@Import(PostgresTestConfiguration.class)
@Transactional   // rollback automatique : chaque test repart d'une base propre
class ProfileModuleTest {

    @Autowired AdminProfileService adminProfileService;
    @Autowired AdvertiserRepository advertiserRepository;
    @Autowired PublisherRepository publisherRepository;

    // ---------- publication de l'événement ----------

    @Test
    void suspendingAnAdvertiserPublishesExactlyOneProfileSuspendedEventWithItsId(PublishedEvents events) {
        // Given
        Long id = activeAdvertiser("contact@acme.com");

        // When
        adminProfileService.updateStatus(id, ProfileStatus.SUSPENDED);

        // Then
        assertThat(events.ofType(ProfileSuspended.class)).hasSize(1);
        assertThat(events.ofType(ProfileSuspended.class).matching(ProfileSuspended::profileId, id)).hasSize(1);
    }

    @Test
    void suspendingAPublisherAlsoPublishesTheEvent(PublishedEvents events) {
        // Given
        Long id = activePublisher("sara@example.com");

        // When
        adminProfileService.updateStatus(id, ProfileStatus.SUSPENDED);

        // Then : c'est aux listeners de décider quoi faire d'un publisher
        assertThat(events.ofType(ProfileSuspended.class).matching(ProfileSuspended::profileId, id)).hasSize(1);
    }

    // ---------- aucune publication ----------

    @Test
    void activatingAPendingAccountPublishesNothing(PublishedEvents events) {
        // Given
        Long id = advertiserRepository.save(newAdvertiser("contact@acme.com")).getId();

        // When
        adminProfileService.updateStatus(id, ProfileStatus.ACTIVE);

        // Then
        assertThat(events.ofType(ProfileSuspended.class)).isEmpty();
    }

    @Test
    void reactivatingASuspendedAccountPublishesNothing(PublishedEvents events) {
        // Given
        Advertiser advertiser = newAdvertiser("contact@acme.com");
        advertiser.activate();
        advertiser.suspend();
        Long id = advertiserRepository.save(advertiser).getId();

        // When
        adminProfileService.updateStatus(id, ProfileStatus.ACTIVE);

        // Then
        assertThat(events.ofType(ProfileSuspended.class)).isEmpty();
    }

    @Test
    void aRefusedSuspensionPublishesNothingAndKeepsTheStatus(PublishedEvents events) {
        // Given : un compte PENDING ne peut pas être suspendu
        Long id = advertiserRepository.save(newAdvertiser("contact@acme.com")).getId();

        // When / Then
        assertThatThrownBy(() -> adminProfileService.updateStatus(id, ProfileStatus.SUSPENDED))
                .isInstanceOf(InvalidStatusTransitionException.class);
        assertThat(events.ofType(ProfileSuspended.class)).isEmpty();
        assertThat(advertiserRepository.findById(id).orElseThrow().getStatus()).isEqualTo(ProfileStatus.PENDING);
    }

    @Test
    void suspendingAnUnknownProfileFailsAndPublishesNothing(PublishedEvents events) {
        assertThatThrownBy(() -> adminProfileService.updateStatus(999_999L, ProfileStatus.SUSPENDED))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(events.ofType(ProfileSuspended.class)).isEmpty();
    }

    // ---------- helpers ----------

    private Advertiser newAdvertiser(String email) {
        return new Advertiser(email, "Acme", "Tech", "https://acme.example.com");
    }

    private Long activeAdvertiser(String email) {
        Advertiser advertiser = newAdvertiser(email);
        advertiser.activate();
        return advertiserRepository.save(advertiser).getId();
    }

    private Long activePublisher(String email) {
        Publisher publisher = new Publisher(email, "Sara", "Alami", Niche.TECH, "bio", "MA");
        publisher.activate();
        return publisherRepository.save(publisher).getId();
    }
}