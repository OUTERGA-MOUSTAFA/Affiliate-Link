package com.affiliateLink.offer.internal.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.affiliateLink.PostgresTestConfiguration;
import com.affiliateLink.common.Niche;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.Tag;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")// rollback mli kat sali test automatique o kay kene les test independant(mosta9il)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)// mashi H2
@Import(PostgresTestConfiguration.class)

class OfferSpecificationsIT {

    private static final long ACME = 1L;
    private static final long NORTH = 2L;

    @Autowired OfferRepository offerRepository;
    @Autowired TagRepository tagRepository;
    @Autowired EntityManager em;

    @BeforeEach
    void givenFourOffers() {
        Tag vpn = tagRepository.save(new Tag("vpn"));
        Tag security = tagRepository.save(new Tag("security"));
        Tag gaming = tagRepository.save(new Tag("gaming"));
        Tag cloud = tagRepository.save(new Tag("cloud"));

        saveOffer(ACME, "Pro VPN", Niche.TECH, vpn, security);
        saveOffer(ACME, "Gaming Mouse", Niche.GAMING, gaming);
        saveOffer(NORTH, "Cloud Backup", Niche.TECH, security, cloud);
        saveOffer(NORTH, "100% Travel_Deals", Niche.TRAVEL);

        em.flush();
        em.clear();
    }

    // ---------- chaque critère seul ----------

    @Test
    void noCriteriaReturnsEveryOffer() {
        assertThat(titles(null, null, null, null))
                .containsExactlyInAnyOrder("Pro VPN", "Gaming Mouse", "Cloud Backup", "100% Travel_Deals");
    }

    @Test
    void nicheAloneKeepsOnlyThatNiche() {
        assertThat(titles(Niche.TECH, null, null, null))
                .containsExactlyInAnyOrder("Pro VPN", "Cloud Backup");
    }

    @Test
    void advertiserAloneKeepsOnlyItsOffers() {
        assertThat(titles(null, ACME, null, null))
                .containsExactlyInAnyOrder("Pro VPN", "Gaming Mouse");
    }

    @Test
    void tagAloneKeepsOnlyOffersCarryingIt() {
        assertThat(titles(null, null, "security", null))
                .containsExactlyInAnyOrder("Pro VPN", "Cloud Backup");
    }

    @Test
    void tagCriterionIgnoresCaseAndSurroundingSpaces() {
        assertThat(titles(null, null, "  VPN ", null)).containsExactly("Pro VPN");
    }

    @Test
    void keywordAloneMatchesTheTitleIgnoringCase() {
        assertThat(titles(null, null, null, "cLoUd")).containsExactly("Cloud Backup");
    }

    // ---------- combinaisons ----------

    @Test
    void nicheAndAdvertiserCombine() {
        assertThat(titles(Niche.TECH, ACME, null, null)).containsExactly("Pro VPN");
    }

    @Test
    void nicheTagAndKeywordCombine() {
        assertThat(titles(Niche.TECH, null, "security", "backup")).containsExactly("Cloud Backup");
    }

    @Test
    void combinationWithNoMatchReturnsNothing() {
        assertThat(titles(Niche.GAMING, NORTH, null, null)).isEmpty();
    }

    @Test
    void blankTagAndKeywordAreIgnored() {
        assertThat(titles(null, null, "  ", "")).hasSize(4);
    }

    // ---------- cas limites ----------

    @Test
    void percentSignInTheKeywordIsLiteralNotAWildcard() {
        assertThat(titles(null, null, null, "%")).containsExactly("100% Travel_Deals");
    }

    @Test
    void underscoreInTheKeywordIsLiteralNotAWildcard() {
        assertThat(titles(null, null, null, "_")).containsExactly("100% Travel_Deals");
    }

    @Test
    void offerWithSeveralTagsIsNotDuplicatedByTheTagJoin() {
        long total = offerRepository
                .findAll(OfferSpecifications.search(null, null, "security", null), PageRequest.of(0, 10))
                .getTotalElements();

        assertThat(total).isEqualTo(2);
    }

    // ---------- helpers ----------

    private void saveOffer(long advertiserId, String title, Niche niche, Tag... tags) {
        Offer offer = new Offer(advertiserId, title, "description", "https://example.com", niche);
        offer.replaceTags(Set.of(tags));
        offerRepository.save(offer);
    }

    private List<String> titles(Niche niche, Long advertiserId, String tag, String keyword) {
        return offerRepository
                .findAll(OfferSpecifications.search(niche, advertiserId, tag, keyword), Sort.by("title"))
                .stream().map(Offer::getTitle).toList();
    }
}