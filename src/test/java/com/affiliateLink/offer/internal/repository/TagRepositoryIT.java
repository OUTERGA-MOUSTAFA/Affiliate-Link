package com.affiliateLink.offer.internal.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.affiliateLink.PostgresTestConfiguration;
import com.affiliateLink.common.Niche;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.Tag;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestConfiguration.class)
class TagRepositoryIT {

    @Autowired TagRepository tagRepository;
    @Autowired OfferRepository offerRepository;
    @Autowired EntityManager em;

    @Test
    void findByNameInReturnsOnlyTheExistingTags() {
        // Given
        tagRepository.saveAll(List.of(new Tag("vpn"), new Tag("security")));

        // When
        List<Tag> found = tagRepository.findByNameIn(Set.of("vpn", "cloud"));

        // Then
        assertThat(found).extracting(Tag::getName).containsExactly("vpn");
    }

    @Test
    void findByNameInWithNoMatchReturnsNothing() {
        tagRepository.save(new Tag("vpn"));

        assertThat(tagRepository.findByNameIn(Set.of("cloud", "gaming"))).isEmpty();
    }

    @Test
    void twoTagsWithTheSameNormalizedNameAreRejected() {
        // Given
        tagRepository.saveAndFlush(new Tag("vpn"));

        // When / Then : " VPN " est normalisé en "vpn"
        assertThatThrownBy(() -> tagRepository.saveAndFlush(new Tag(" VPN ")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aTagIsSharedBetweenOffersInsteadOfBeingDuplicated() {
        // Given
        Tag vpn = tagRepository.save(new Tag("vpn"));
        Offer first = new Offer(1L, "First", "d", "https://example.com", Niche.TECH);
        Offer second = new Offer(2L, "Second", "d", "https://example.com", Niche.TECH);
        first.replaceTags(Set.of(vpn));
        second.replaceTags(Set.of(vpn));
        offerRepository.saveAll(List.of(first, second));
        em.flush();
        em.clear();

        // When
        List<Offer> offers = offerRepository.findAll();

        // Then
        assertThat(tagRepository.count()).isEqualTo(1);
        assertThat(offers).hasSize(2)
                .allSatisfy(offer -> assertThat(offer.getTags()).extracting(Tag::getName).containsExactly("vpn"));
    }
}