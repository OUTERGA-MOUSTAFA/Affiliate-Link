package com.affiliateLink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestConfiguration.class)
class MigrationsIT {

    @Autowired EntityManager em;

    // ---------- Flyway ----------

    @Test
    void allMigrationsAreAppliedOnAnEmptyDatabase() {
        List<String> versions = strings(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank");

        assertThat(versions).contains("1", "2", "3", "4", "5", "6", "7", "8");   // ajoute les suivantes ici
        assertThat(strings("SELECT CAST(COUNT(*) AS varchar) FROM flyway_schema_history WHERE success = false"))
                .containsExactly("0");
    }

    @Test
    void everyExpectedTableExists() {
        assertThat(strings("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"))
                .contains("profile_profiles", "profile_publishers", "profile_advertisers", "event_publication",
                        "offer_offers", "offer_campaigns", "offer_tags", "offer_offer_tags");
    }

    // ---------- offer_campaigns : contraintes CHECK ----------

    @ParameterizedTest(name = "refused: {0}")
    @CsvSource(delimiter = '|', value = {
            "end date equal to start date|0|1000|CPC|2|DRAFT",
            "end date before start date|-1|1000|CPC|2|DRAFT",
            "zero budget|10|0|CPC|2|DRAFT",
            "negative budget|10|-5|CPC|2|DRAFT",
            "zero payout|10|1000|CPC|0|DRAFT",
            "unknown payout model|10|1000|CPM|2|DRAFT",
            "unknown status|10|1000|CPC|2|DONE",
            "percentage above 100|10|1000|CPA_PERCENT|100.01|DRAFT"
    })
    void invalidCampaignRowsAreRejected(String reason, int endOffsetDays, String budget, String model,
            String payout, String status) {
        long offerId = insertOffer("TECH");

        assertThatThrownBy(() -> insertCampaign(offerId, endOffsetDays, budget, model, payout, status))
                .isInstanceOf(PersistenceException.class)
                .rootCause().hasMessageContaining("check constraint");
    }

    @ParameterizedTest(name = "accepted: {0}")
    @CsvSource(delimiter = '|', value = {
            "fixed amount above 100|10|1000|CPC|150|DRAFT",
            "percentage equal to 100|10|1000|CPA_PERCENT|100|DRAFT",
            "smallest positive percentage|10|1000|CPA_PERCENT|0.01|ACTIVE",
            "every payout model|10|1000|CPA_FIXED|75|PAUSED"
    })
    void validCampaignRowsAreAccepted(String reason, int endOffsetDays, String budget, String model,
            String payout, String status) {
        long offerId = insertOffer("TECH");

        assertThatCode(() -> insertCampaign(offerId, endOffsetDays, budget, model, payout, status))
                .doesNotThrowAnyException();
    }

    // ---------- offer_offers, offer_tags, offer_offer_tags ----------

    @Test
    void anOfferWithAnUnknownNicheIsRejected() {
        assertThatThrownBy(() -> insertOffer("CRYPTO"))
                .isInstanceOf(PersistenceException.class)
                .rootCause().hasMessageContaining("check constraint");
    }

    @Test
    void anUppercaseTagNameIsRejected() {
        assertThatThrownBy(() -> insertTag("VPN"))
                .isInstanceOf(PersistenceException.class)
                .rootCause().hasMessageContaining("check constraint");
    }

    @Test
    void aDuplicateTagNameIsRejected() {
        insertTag("vpn");

        assertThatThrownBy(() -> insertTag("vpn"))
                .isInstanceOf(PersistenceException.class)
                .rootCause().hasMessageContaining("duplicate key");
    }

    @Test
    void theSameTagCannotBeLinkedTwiceToTheSameOffer() {
        long offerId = insertOffer("TECH");
        long tagId = insertTag("vpn");
        link(offerId, tagId);

        assertThatThrownBy(() -> link(offerId, tagId))
                .isInstanceOf(PersistenceException.class)
                .rootCause().hasMessageContaining("duplicate key");
    }

    @Test
    void deletingAnOfferDeletesItsTagLinks() {
        // Given
        long offerId = insertOffer("TECH");
        link(offerId, insertTag("vpn"));

        // When
        em.createNativeQuery("DELETE FROM offer_offers WHERE id = ?1").setParameter(1, offerId).executeUpdate();

        // Then : ON DELETE CASCADE
        assertThat(count("SELECT COUNT(*) FROM offer_offer_tags WHERE offer_id = " + offerId)).isZero();
    }

    @Test
    void aTagStillUsedByAnOfferCannotBeDeleted() {
        long offerId = insertOffer("TECH");
        long tagId = insertTag("vpn");
        link(offerId, tagId);

        assertThatThrownBy(() -> em.createNativeQuery("DELETE FROM offer_tags WHERE id = ?1")
                .setParameter(1, tagId).executeUpdate())
                .isInstanceOf(PersistenceException.class)
                .rootCause().hasMessageContaining("foreign key");
    }

    @Test
    void aCampaignCannotReferenceAnOfferThatDoesNotExist() {
        assertThatThrownBy(() -> insertCampaign(999_999L, 10, "1000", "CPC", "2", "DRAFT"))
                .isInstanceOf(PersistenceException.class)
                .rootCause().hasMessageContaining("foreign key");
    }

    // ---------- helpers SQL ----------

    private long insertOffer(String niche) {
        Object id = em.createNativeQuery("""
                INSERT INTO offer_offers (advertiser_id, title, landing_url, niche)
                VALUES (1, 'Pro VPN', 'https://example.com', ?1) RETURNING id
                """).setParameter(1, niche).getSingleResult();
        return ((Number) id).longValue();
    }

    private long insertTag(String name) {
        Object id = em.createNativeQuery("INSERT INTO offer_tags (name) VALUES (?1) RETURNING id")
                .setParameter(1, name).getSingleResult();
        return ((Number) id).longValue();
    }

    private void link(long offerId, long tagId) {
        em.createNativeQuery("INSERT INTO offer_offer_tags (offer_id, tag_id) VALUES (?1, ?2)")
                .setParameter(1, offerId).setParameter(2, tagId).executeUpdate();
    }

    private void insertCampaign(long offerId, int endOffsetDays, String budget, String model,
            String payout, String status) {
        LocalDate start = LocalDate.now();
        em.createNativeQuery("""
                INSERT INTO offer_campaigns
                    (offer_id, name, start_date, end_date, budget, payout_model, payout_value, status)
                VALUES (?1, 'C', ?2, ?3, CAST(?4 AS numeric), ?5, CAST(?6 AS numeric), ?7)
                """)
                .setParameter(1, offerId)
                .setParameter(2, start)
                .setParameter(3, start.plusDays(endOffsetDays))
                .setParameter(4, budget)
                .setParameter(5, model)
                .setParameter(6, payout)
                .setParameter(7, status)
                .executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private List<String> strings(String sql) {
        return em.createNativeQuery(sql).getResultList();
    }

    private long count(String sql) {
        em.flush();
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }
}