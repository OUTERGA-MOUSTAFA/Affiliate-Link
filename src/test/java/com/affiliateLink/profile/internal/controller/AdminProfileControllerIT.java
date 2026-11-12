package com.affiliateLink.profile.internal.controller;

import static com.affiliateLink.ProblemDetailMatchers.problem;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.service.AdminProfileService;

@WebMvcTest(AdminProfileController.class)
class AdminProfileControllerIT {

    @Autowired MockMvc mockMvc;
    @MockitoBean AdminProfileService adminProfileService;

    @Test
    void changingTheStatusReturns204AndCallsTheService() throws Exception {
        mockMvc.perform(patch("/api/admin/profiles/3/status")
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isNoContent());

        verify(adminProfileService).updateStatus(3L, ProfileStatus.SUSPENDED);
    }

    @Test
    void anUnknownProfileReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Profil introuvable avec l'id : 99"))
                .when(adminProfileService).updateStatus(99L, ProfileStatus.ACTIVE);

        mockMvc.perform(patch("/api/admin/profiles/99/status")
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
                .andExpect(problem(HttpStatus.NOT_FOUND));
    }

    @Test
    void aForbiddenTransitionReturns409() throws Exception {
        doThrow(new InvalidStatusTransitionException("Impossible de passer de ACTIVE à ACTIVE"))
                .when(adminProfileService).updateStatus(3L, ProfileStatus.ACTIVE);

        mockMvc.perform(patch("/api/admin/profiles/3/status")
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
                .andExpect(problem(HttpStatus.CONFLICT));
    }

    @Test
    void anUnknownStatusValueReturns400() throws Exception {
        mockMvc.perform(patch("/api/admin/profiles/3/status")
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"XYZ\"}"))
                .andExpect(problem(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(adminProfileService);
    }

    @Test
    void aMissingStatusReturns400() throws Exception {
        mockMvc.perform(patch("/api/admin/profiles/3/status")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(problem(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(adminProfileService);
    }
}