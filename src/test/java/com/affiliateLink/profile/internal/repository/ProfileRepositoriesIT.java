package com.affiliateLink.profile.internal.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.affiliateLink.PostgresTestConfiguration;
import com.affiliateLink.common.Niche;
import com.affiliateLink.profile.internal.entity.Advertiser;
import com.affiliateLink.profile.internal.entity.Profile;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.entity.Publisher;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestConfiguration.class)
class ProfileRepositoriesIT {

    @Autowired ProfileRepository profileRepository;
    @Autowired PublisherRepository publisherRepository;
    @Autowired AdvertiserRepository advertiserRepository;
    @Autowired EntityManager em;

    private Publisher publisher(String email) {
        return new Publisher(email, "Sara", "Alami", Niche.TECH, "bio", "MA");
    }

    private Advertiser advertiser(String email) {
        return new Advertiser(email, "Acme", "Tech", "https://acme.example.com");
    }

    @Test
    void existsByEmailIsTrueForAPublisherAndForAnAdvertiser() {
        // Given
        publisherRepository.save(publisher("sara@example.com"));
        advertiserRepository.save(advertiser("contact@acme.com"));

        // Then : la table commune couvre les deux types de profil
        assertThat(profileRepository.existsByEmail("sara@example.com")).isTrue();
        assertThat(profileRepository.existsByEmail("contact@acme.com")).isTrue();
    }

    @Test
    void existsByEmailIsFalseForAnUnknownEmail() {
        assertThat(profileRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void theDatabaseRejectsTheSameEmailForAPublisherAndAnAdvertiser() {
        // Given
        publisherRepository.saveAndFlush(publisher("same@example.com"));

        // When / Then
        assertThatThrownBy(() -> advertiserRepository.saveAndFlush(advertiser("same@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aNewProfileIsStoredAsPending() {
        // Given
        Long id = publisherRepository.saveAndFlush(publisher("sara@example.com")).getId();
        em.clear();

        // When
        Profile reloaded = profileRepository.findById(id).orElseThrow();

        // Then
        assertThat(reloaded.getStatus()).isEqualTo(ProfileStatus.PENDING);
    }

    @Test
    void findingByTheParentRepositoryReturnsTheRightSubclass() {
        // Given : héritage JOINED
        Long publisherId = publisherRepository.saveAndFlush(publisher("sara@example.com")).getId();
        Long advertiserId = advertiserRepository.saveAndFlush(advertiser("contact@acme.com")).getId();
        em.clear();

        // Then
        assertThat(profileRepository.findById(publisherId).orElseThrow()).isInstanceOf(Publisher.class);
        assertThat(profileRepository.findById(advertiserId).orElseThrow()).isInstanceOf(Advertiser.class);
    }

    @Test
    void activePublishersAreFoundByStatus() {
        // Given
        Publisher active = publisher("active@example.com");
        active.activate();
        publisherRepository.save(active);
        publisherRepository.save(publisher("pending@example.com"));
        em.flush();
        em.clear();

        // When : adapte le nom de la méthode à ton PublisherRepository
        List<Publisher> found = publisherRepository.findByStatus(ProfileStatus.ACTIVE);

        // Then
        assertThat(found).extracting(Profile::getEmail).containsExactly("active@example.com");
    }
}