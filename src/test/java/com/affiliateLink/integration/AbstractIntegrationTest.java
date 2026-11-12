package com.affiliateLink.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.affiliateLink.PostgresTestConfiguration;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
// base class كتفرغ الـ database قبل كل test بـ TRUNCATE ... RESTART IDENTITY
// CASCADE. يعني ما كاين حتى test كيعتمد على data ديال آخر ولا على الترتيب.
abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    // (la méthode d'une classe parente s'exécute avant celle de la classe fille)
    @BeforeEach
    void startFromAnEmptyDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE offer_offer_tags, offer_campaigns, offer_offers, offer_tags,
                               profile_publishers, profile_advertisers, profile_profiles, event_publication
                RESTART IDENTITY CASCADE""");
    }

    protected long count(String sql) {
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    protected long idOf(ResultActions result) throws Exception {
        String json = result.andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    protected String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }

    // ---------- profile ----------

    protected ResultActions postAdvertiser(String email, String company) throws Exception {
        return mockMvc.perform(post("/api/advertisers").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","nomEntreprise":"%s","secteur":"Tech","siteWeb":"https://example.com"}
                """.formatted(email, company)));
    }

    protected long createAdvertiser(String email, String company) throws Exception {
        return idOf(postAdvertiser(email, company).andExpect(status().isCreated()));
    }

    protected ResultActions postPublisher(String email) throws Exception {
        return mockMvc.perform(post("/api/publishers").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","firstName":"Sara","lastName":"Alami","niche":"TECH","bio":"bio","country":"MA"}
                """.formatted(email)));
    }

    protected long createPublisher(String email) throws Exception {
        return idOf(postPublisher(email).andExpect(status().isCreated()));
    }

    protected ResultActions patchProfileStatus(long id, String target) throws Exception {
        return mockMvc.perform(patch("/api/admin/profiles/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + target + "\"}"));
    }

    protected void setProfileStatus(long id, String target) throws Exception {
        patchProfileStatus(id, target).andExpect(status().isNoContent());
    }

    protected long activeAdvertiser(String company) throws Exception {
        long id = createAdvertiser(uniqueEmail(), company);
        setProfileStatus(id, "ACTIVE");
        return id;
    }

    protected long activePublisher() throws Exception {
        long id = createPublisher(uniqueEmail());
        setProfileStatus(id, "ACTIVE");
        return id;
    }

    // ---------- offer ----------

    protected ResultActions postOffer(long advertiserId, String title, String niche, String... tags) throws Exception {
        String tagsJson = Arrays.stream(tags).map(tag -> "\"" + tag + "\"").collect(Collectors.joining(",", "[", "]"));
        return mockMvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON).content(
                """
                        {"advertiserId":%d,"title":"%s","description":"d","landingUrl":"https://example.com","niche":"%s","tags":%s}
                        """
                        .formatted(advertiserId, title, niche, tagsJson)));
    }

    protected long createOffer(long advertiserId, String title, String niche, String... tags) throws Exception {
        return idOf(postOffer(advertiserId, title, niche, tags).andExpect(status().isCreated()));
    }

    // ---------- campaign ----------

    protected ResultActions postCampaign(long offerId, String name, LocalDate start, LocalDate end,
            String model, String payoutValue) throws Exception {
        return mockMvc.perform(post("/api/offres/{id}/campaigns", offerId)
                .contentType(MediaType.APPLICATION_JSON).content(
                        """
                                {"name":"%s","startDate":"%s","endDate":"%s","budget":1000.00,"payoutModel":"%s","payoutValue":%s}
                                """
                                .formatted(name, start, end, model, payoutValue)));
    }

    protected long createCampaign(long offerId, String name) throws Exception {
        return idOf(postCampaign(offerId, name, LocalDate.now(), LocalDate.now().plusDays(30), "CPC", "0.50")
                .andExpect(status().isCreated()));
    }

    protected ResultActions patchCampaignStatus(long id, String target) throws Exception {
        return mockMvc.perform(patch("/api/campaigns/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + target + "\"}"));
    }

    protected void changeCampaignStatus(long id, String target) throws Exception {
        patchCampaignStatus(id, target).andExpect(status().isNoContent());
    }
}