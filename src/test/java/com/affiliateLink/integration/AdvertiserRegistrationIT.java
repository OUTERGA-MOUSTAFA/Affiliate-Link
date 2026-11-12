package com.affiliateLink.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AdvertiserRegistrationIT extends AbstractIntegrationTest {

    // ============ AF-142 : inscription puis consultation ============

    @Test
    void anAdvertiserRegistersAsPendingThenItsProfileCanBeConsulted() throws Exception {
        // When
        long id = idOf(postAdvertiser("contact@acme.com", "Acme")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("contact@acme.com"))
                .andExpect(jsonPath("$.status").value("PENDING")));

        // Then
        mockMvc.perform(get("/api/advertisers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("contact@acme.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void aRegisteredAdvertiserAppearsInTheList() throws Exception {
        createAdvertiser("contact@acme.com", "Acme");

        mockMvc.perform(get("/api/advertisers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void consultingAnUnknownProfileReturns404() throws Exception {
        mockMvc.perform(get("/api/advertisers/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void anInvalidRegistrationIsRefusedAndCreatesNoProfile() throws Exception {
        postAdvertiser("not-an-email", "Acme").andExpect(status().isBadRequest());

        Assertions.assertThat(count("SELECT COUNT(*) FROM profile_profiles")).isZero();
    }

    // ============ AF-143 : double inscription ============

    @Test
    void registeringTwiceWithTheSameEmailIsRefusedAndCreatesNothing() throws Exception {
        // Given
        createAdvertiser("contact@acme.com", "Acme");

        // When / Then
        postAdvertiser("contact@acme.com", "Another company")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        Assertions.assertThat(count("SELECT COUNT(*) FROM profile_profiles")).isEqualTo(1);
        Assertions.assertThat(count("SELECT COUNT(*) FROM profile_advertisers")).isEqualTo(1);
    }

    @Test
    void anEmailAlreadyUsedByAPublisherCannotRegisterAnAdvertiser() throws Exception {
        createPublisher("shared@example.com");

        postAdvertiser("shared@example.com", "Acme").andExpect(status().isConflict());

        Assertions.assertThat(count("SELECT COUNT(*) FROM profile_advertisers")).isZero();
    }

    @Test
    void anEmailAlreadyUsedByAnAdvertiserCannotRegisterAPublisher() throws Exception {
        createAdvertiser("shared@example.com", "Acme");

        postPublisher("shared@example.com").andExpect(status().isConflict());

        Assertions.assertThat(count("SELECT COUNT(*) FROM profile_publishers")).isZero();
    }
}