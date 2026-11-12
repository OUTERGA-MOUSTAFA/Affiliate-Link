package com.affiliateLink.integration;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class OfferSearchIT extends AbstractIntegrationTest {

    private long acme;
    private long north;

    @BeforeEach
    void given15Offers() throws Exception {
        acme = activeAdvertiser("Acme");
        north = activeAdvertiser("North");

        for (int i = 1; i <= 12; i++) {
            createOffer(acme, String.format("Offer %02d", i), "TECH", i % 2 == 1 ? "vpn" : "cloud");
        }
        createOffer(north, "Game 1", "GAMING", "gaming", "vpn");
        createOffer(north, "Game 2", "GAMING", "gaming");
        createOffer(north, "Game 3", "GAMING", "gaming");
    }

    private ResultActions search(String... keyValues) throws Exception {
        MockHttpServletRequestBuilder request = get("/api/offres");
        for (int i = 0; i < keyValues.length; i += 2) {
            request.param(keyValues[i], keyValues[i + 1]);
        }
        return mockMvc.perform(request);
    }

    // ---------- pagination ----------

    @Test
    void withoutCriteriaEveryOfferIsReturnedPageByPage() throws Exception {
        search("size", "5").andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void theLastPageHoldsTheRemainder() throws Exception {
        search("page", "2", "size", "5", "sort", "title,asc")
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.content[*].title",
                        contains("Offer 08", "Offer 09", "Offer 10", "Offer 11", "Offer 12")));
    }

    @Test
    void aPageBeyondTheEndIsEmptyButKeepsTheTotals() throws Exception {
        search("page", "9", "size", "5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(15));
    }

    // ---------- tri ----------

    @Test
    void withoutSortTheNewestOfferComesFirst() throws Exception {
        search("size", "3").andExpect(jsonPath("$.content[*].title", contains("Game 3", "Game 2", "Game 1")));
    }

    @Test
    void sortByTitleAscending() throws Exception {
        search("sort", "title,asc", "size", "4")
                .andExpect(jsonPath("$.content[*].title", contains("Game 1", "Game 2", "Game 3", "Offer 01")));
    }

    @Test
    void sortByTitleDescending() throws Exception {
        search("sort", "title,desc", "size", "2")
                .andExpect(jsonPath("$.content[*].title", contains("Offer 12", "Offer 11")));
    }

    // ---------- chaque critère seul ----------

    @Test
    void filterByNiche() throws Exception {
        search("niche", "GAMING").andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void filterByAdvertiser() throws Exception {
        search("advertiserId", String.valueOf(acme)).andExpect(jsonPath("$.totalElements").value(12));
        search("advertiserId", String.valueOf(north)).andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void filterByTagIgnoresCase() throws Exception {
        // vpn : 6 offres Acme impaires + Game 1
        search("tag", "VPN").andExpect(jsonPath("$.totalElements").value(7));
    }

    @Test
    void filterByKeywordInTheTitleIgnoresCase() throws Exception {
        search("keyword", "game").andExpect(jsonPath("$.totalElements").value(3));
        search("keyword", "OFFER").andExpect(jsonPath("$.totalElements").value(12));
    }

    // ---------- combinaisons ----------

    @Test
    void criteriaCombine() throws Exception {
        search("niche", "TECH", "tag", "vpn").andExpect(jsonPath("$.totalElements").value(6));
        search("niche", "GAMING", "tag", "vpn").andExpect(jsonPath("$.totalElements").value(1));
        search("advertiserId", String.valueOf(north), "keyword", "game 2")
                .andExpect(jsonPath("$.content[*].title", contains("Game 2")));
    }

    @Test
    void aCombinationWithNoMatchReturnsAnEmptyPage() throws Exception {
        search("niche", "TRAVEL", "keyword", "offer")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ---------- erreurs ----------

    @Test
    void anUnsupportedSortFieldReturns400() throws Exception {
        search("sort", "password,asc").andExpect(status().isBadRequest());
    }

    @Test
    void anUnknownNicheReturns400() throws Exception {
        search("niche", "XYZ").andExpect(status().isBadRequest());
    }
}