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
import com.affiliateLink.common.Niche;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.internal.dto.PublisherCreateRequest;
import com.affiliateLink.profile.internal.entity.Publisher;
import com.affiliateLink.profile.internal.service.PublisherService;

@WebMvcTest(PublisherController.class)
class PublisherControllerTest {

    private static final String VALID_BODY = """
            {"email":"sara@example.com","firstName":"Sara","lastName":"Alami",
             "niche":"TECH","bio":"Tech reviewer","country":"MA"}""";

    @Autowired MockMvc mockMvc;
    @MockitoBean PublisherService publisherService;

    private Publisher publisher() {
        Publisher publisher = new Publisher("sara@example.com", "Sara", "Alami", Niche.TECH, "Tech reviewer", "MA");
        ReflectionTestUtils.setField(publisher, "id", 1L);
        return publisher;
    }

    @Test
    void createReturns201WithAPendingPublisher() throws Exception {
        when(publisherService.create(any(PublisherCreateRequest.class))).thenReturn(publisher());

        mockMvc.perform(post("/api/publishers").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("sara@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createWithAnInvalidEmailReturns400AndCreatesNothing() throws Exception {
        String body = VALID_BODY.replace("sara@example.com", "not-an-email");

        mockMvc.perform(post("/api/publishers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(problem(HttpStatus.BAD_REQUEST))
                .andExpect(detailContains("email"));

        verifyNoInteractions(publisherService);
    }

    @Test
    void createWithAMissingFirstNameReturns400() throws Exception {
        String body = VALID_BODY.replace("\"firstName\":\"Sara\"", "\"firstName\":\"\"");

        mockMvc.perform(post("/api/publishers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(problem(HttpStatus.BAD_REQUEST))
                .andExpect(detailContains("firstName"));
    }

    @Test
    void createWithAnUnknownNicheReturns400() throws Exception {
        String body = VALID_BODY.replace("\"TECH\"", "\"CRYPTO\"");

        mockMvc.perform(post("/api/publishers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(problem(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(publisherService);
    }

    @Test
    void createWithAnEmailAlreadyUsedReturns409() throws Exception {
        when(publisherService.create(any(PublisherCreateRequest.class)))
                .thenThrow(new DuplicateEmailException("Un compte existe déjà avec l'email : sara@example.com"));

        mockMvc.perform(post("/api/publishers").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(problem(HttpStatus.CONFLICT))
                .andExpect(detailContains("sara@example.com"));
    }

    @Test
    void findByIdReturns200() throws Exception {
        when(publisherService.findById(1L)).thenReturn(publisher());

        mockMvc.perform(get("/api/publishers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Sara"));
    }

    @Test
    void findByIdOfAnUnknownPublisherReturns404() throws Exception {
        when(publisherService.findById(99L)).thenThrow(new ResourceNotFoundException("Publisher introuvable avec l'id : 99"));

        mockMvc.perform(get("/api/publishers/99"))
                .andExpect(problem(HttpStatus.NOT_FOUND))
                .andExpect(detailContains("99"));
    }

    @Test
    void findAllReturns200WithTheList() throws Exception {
        when(publisherService.findAll()).thenReturn(List.of(publisher()));

        mockMvc.perform(get("/api/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}