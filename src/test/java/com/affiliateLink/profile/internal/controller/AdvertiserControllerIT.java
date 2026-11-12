package com.affiliateLink.profile.internal.controller;

import static com.affiliateLink.ProblemDetailMatchers.detailContains;
import static com.affiliateLink.ProblemDetailMatchers.problem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.affiliateLink.common.DuplicateEmailException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.internal.dto.AdvertiserCreateRequest;
import com.affiliateLink.profile.internal.entity.Advertiser;
import com.affiliateLink.profile.internal.service.AdvertiserService;

@WebMvcTest(AdvertiserController.class)
class AdvertiserControllerIT {

    private static final String VALID_BODY = """
            {"email":"contact@acme.com","nomEntreprise":"Acme","secteur":"Tech",
             "siteWeb":"https://acme.example.com"}""";

    @Autowired MockMvc mockMvc;
    @MockitoBean AdvertiserService advertiserService;

    private Advertiser advertiser() {
        Advertiser advertiser = new Advertiser("contact@acme.com", "Acme", "Tech", "https://acme.example.com");
        ReflectionTestUtils.setField(advertiser, "id", 3L);
        return advertiser;
    }

    @Test
    void createReturns201WithAPendingAdvertiser() throws Exception {
        when(advertiserService.creaAdvertiser(any(AdvertiserCreateRequest.class))).thenReturn(advertiser());

        mockMvc.perform(post("/api/advertisers").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createWithAnInvalidEmailReturns400AndCreatesNothing() throws Exception {
        String body = VALID_BODY.replace("contact@acme.com", "not-an-email");

        mockMvc.perform(post("/api/advertisers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(problem(HttpStatus.BAD_REQUEST))
                .andExpect(detailContains("email"));

        verifyNoInteractions(advertiserService);
    }

    @Test
    void createWithABlankCompanyNameReturns400() throws Exception {
        String body = VALID_BODY.replace("\"nomEntreprise\":\"Acme\"", "\"nomEntreprise\":\"\"");

        mockMvc.perform(post("/api/advertisers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(problem(HttpStatus.BAD_REQUEST))
                .andExpect(detailContains("nomEntreprise"));
    }

    @Test
    void createWithAnEmailAlreadyUsedReturns409() throws Exception {
        when(advertiserService.creaAdvertiser(any(AdvertiserCreateRequest.class)))
                .thenThrow(new DuplicateEmailException("Un compte existe déjà avec l'email : contact@acme.com"));

        mockMvc.perform(post("/api/advertisers").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(problem(HttpStatus.CONFLICT));
    }

    @Test
    void findByIdReturns200() throws Exception {
        when(advertiserService.findById(3L)).thenReturn(advertiser());

        mockMvc.perform(get("/api/advertisers/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("contact@acme.com"));
    }

    @Test
    void findByIdOfAnUnknownAdvertiserReturns404() throws Exception {
        when(advertiserService.findById(99L)).thenThrow(new ResourceNotFoundException("Advertiser introuvable avec l'id : 99"));

        mockMvc.perform(get("/api/advertisers/99")).andExpect(problem(HttpStatus.NOT_FOUND));
    }

    @Test
    void findAllReturns200WithTheList() throws Exception {
        when(advertiserService.findAll()).thenReturn(List.of(advertiser()));

        mockMvc.perform(get("/api/advertisers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}