package com.affiliateLink.offer.internal.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.affiliateLink.PostgresTestConfiguration;
import com.affiliateLink.common.Niche;
import com.affiliateLink.offer.internal.entity.Offer;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestConfiguration.class)
class OfferPaginationIT {

    @Autowired OfferRepository offerRepository;
    @Autowired EntityManager em;

    // 25 offres "Offer 01".."Offer 25" ; une offre sur cinq est TECH (5, 10, 15, 20, 25)
    @BeforeEach
    void given25Offers() {
        Instant base = Instant.parse("2026-01-01T00:00:00Z");
        for (int i = 1; i <= 25; i++) {
            Offer offer = new Offer(1L, String.format("Offer %02d", i), "d", "https://example.com",
                    i % 5 == 0 ? Niche.TECH : Niche.GAMING);
            ReflectionTestUtils.setField(offer, "createdAt", base.plusSeconds(i));   // dates distinctes et connues
            offerRepository.save(offer);
        }
        em.flush();
        em.clear();
    }

    private Page<Offer> page(int number, int size, Sort sort, Niche niche) {
        return offerRepository.findAll(OfferSpecifications.search(niche, null, null, null),
                PageRequest.of(number, size, sort));
    }

    @Test
    void firstPageHasTheRequestedSizeAndTheTotals() {
        Page<Offer> page = page(0, 10, Sort.by("title"), null);

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(25);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    void lastPageHoldsTheRemainder() {
        assertThat(page(2, 10, Sort.by("title"), null).getContent()).hasSize(5);
    }

    @Test
    void pageBeyondTheEndIsEmptyButKeepsTheTotal() {
        Page<Offer> page = page(5, 10, Sort.by("title"), null);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(25);
    }

    @Test
    void pagesNeverOverlapAndTogetherCoverEveryOffer() {
        Set<Long> ids = new HashSet<>();
        for (int number = 0; number < 3; number++) {
            page(number, 10, Sort.by("title"), null).forEach(offer -> ids.add(offer.getId()));
        }

        assertThat(ids).hasSize(25);
    }

    @Test
    void sortByTitleAscendingStartsWithTheFirstTitle() {
        assertThat(page(0, 3, Sort.by("title").ascending(), null).getContent())
                .extracting(Offer::getTitle).containsExactly("Offer 01", "Offer 02", "Offer 03");
    }

    @Test
    void sortByTitleDescendingStartsWithTheLastTitle() {
        assertThat(page(0, 3, Sort.by("title").descending(), null).getContent())
                .extracting(Offer::getTitle).containsExactly("Offer 25", "Offer 24", "Offer 23");
    }

    @Test
    void sortByCreationDateDescendingReturnsTheMostRecentFirst() {
        assertThat(page(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"), null).getContent())
                .extracting(Offer::getTitle).containsExactly("Offer 25", "Offer 24");
    }

    @Test
    void totalsReflectTheFilterNotTheWholeTable() {
        Page<Offer> page = page(0, 2, Sort.by("title"), Niche.TECH);

        assertThat(page.getContent()).extracting(Offer::getTitle).containsExactly("Offer 05", "Offer 10");
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }
}