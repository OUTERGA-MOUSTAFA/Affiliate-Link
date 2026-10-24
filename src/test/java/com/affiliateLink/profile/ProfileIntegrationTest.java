package com.affiliateLink.profile;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional   // كل تيست كيرجع للوراء (rollback) أوتوماتيكيا، بلا ما يلوث القاعدة
class ProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
   private final JsonMapper objectMapper = new JsonMapper();

    @Test
    void creerUnPublisherDoitRetourner201EtStatusPending() throws Exception {
        String body = """
                {
                  "email": "integration.test1@mail.com",
                  "firstName": "Redouane",
                  "lastName": "Benali",
                  "niche": "TECH",
                  "bio": "bio test",
                  "country": "Maroc"
                }
                """;

        mockMvc.perform(post("/api/publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.email").value("integration.test1@mail.com"));
    }

    @Test
    void creerDeuxPublishersAvecMemeEmailDoitRetourner409() throws Exception {
        String body = """
                {
                  "email": "integration.test2@mail.com",
                  "firstName": "Said",
                  "lastName": "Benali",
                  "niche": "TECH",
                  "bio": "bio",
                  "country": "Maroc"
                }
                """;

        // première création : OK
        mockMvc.perform(post("/api/publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // deuxième création, même email : conflit
        mockMvc.perform(post("/api/publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void creerPublisherAvecEmailInvalideDoitRetourner400() throws Exception {
        String body = """
                {
                  "email": "pas-un-email",
                  "firstName": "",
                  "lastName": "Benali",
                  "niche": "TECH",
                  "country": "Maroc"
                }
                """;

        mockMvc.perform(post("/api/publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cycleCompletDeTransitionsDeStatut() throws Exception {
        // 1. créer un publisher -> PENDING
        String createBody = """
                {
                  "email": "integration.test3@mail.com",
                  "firstName": "Sara",
                  "lastName": "Idrissi",
                  "niche": "FOOD",
                  "bio": "bio",
                  "country": "Maroc"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long id = created.get("id").asLong();

        // 2. PENDING -> ACTIVE : autorisé
        mockMvc.perform(patch("/api/admin/profiles/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACTIVE\"}"))
                .andExpect(status().isNoContent());

        // 3. ACTIVE -> SUSPENDED : autorisé
        mockMvc.perform(patch("/api/admin/profiles/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"SUSPENDED\"}"))
                .andExpect(status().isNoContent());

        // 4. SUSPENDED -> ACTIVE : autorisé
        mockMvc.perform(patch("/api/admin/profiles/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACTIVE\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void transitionInterditeDoitRetourner409() throws Exception {
        String createBody = """
                {
                  "email": "integration.test4@mail.com",
                  "firstName": "Youssef",
                  "lastName": "Alami",
                  "niche": "SPORT",
                  "bio": "bio",
                  "country": "Maroc"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/publisher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long id = created.get("id").asLong();

        // PENDING -> SUSPENDED directement : interdit
        mockMvc.perform(patch("/api/admin/profiles/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"SUSPENDED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getProfilInexistantDoitRetourner404() throws Exception {
        mockMvc.perform(get("/api/publishers/999999"))
                .andExpect(status().isNotFound());
    }
}