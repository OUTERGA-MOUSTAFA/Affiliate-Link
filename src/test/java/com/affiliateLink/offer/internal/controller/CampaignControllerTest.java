package com.affiliateLink.offer.internal.controller;

import static com.affiliateLink.ProblemDetailMatchers.detail;
import static com.affiliateLink.ProblemDetailMatchers.detailContains;
import static com.affiliateLink.ProblemDetailMatchers.problem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.affiliateLink.common.ConflictException;
import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.internal.dto.CampaignCreateRequest;
import com.affiliateLink.offer.internal.dto.CampaignResponse;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.entity.PayoutModel;
import com.affiliateLink.offer.internal.service.CampaignService;

@WebMvcTest(CampaignController.class)
class CampaignControllerTest {

    private static final String VALID = """
            {"name":"Winter Sale","startDate":"2026-11-10","endDate":"2027-01-31",
             "budget":1000.00,"payoutModel":"CPC","payoutValue":0.50}""";

    @Autowired MockMvc mockMvc;
    @MockitoBean CampaignService campaignService;

    private CampaignResponse campaign(CampaignStatus status) {
        return new CampaignResponse(5L, 1L, "Winter Sale", LocalDate.parse("2026-11-10"),
                LocalDate.parse("2027-01-31"), new BigDecimal("1000.00"), PayoutModel.CPC,
                new BigDecimal("0.50"), status);
    }

    // ---------- POST /api/offres/{id}/campaigns ----------

    @Test
    void createReturns201WithADraftCampaign() throws Exception {
        when(campaignService.create(eq(1L), any(CampaignCreateRequest.class))).thenReturn(campaign(CampaignStatus.DRAFT));

        mockMvc.perform(post("/api/offres/1/campaigns").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.offerId").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    static Stream<Arguments> invalidCampaignBodies() {
        return Stream.of(
                Arguments.of("end date before start date", VALID.replace("2027-01-31", "2026-11-01"), "endDate must be after startDate"),
                Arguments.of("end date equal to start date", VALID.replace("2027-01-31", "2026-11-10"), "endDate must be after startDate"),
                Arguments.of("percentage above 100",
                        VALID.replace("\"CPC\"", "\"CPA_PERCENT\"").replace("0.50", "150"), "percentage"),
                Arguments.of("zero budget", VALID.replace("1000.00", "0"), "budget"),
                Arguments.of("negative budget", VALID.replace("1000.00", "-10"), "budget"),
                Arguments.of("zero payout", VALID.replace("0.50", "0"), "payoutValue"),
                Arguments.of("budget with 3 decimals", VALID.replace("1000.00", "1000.123"), "budget"),
                Arguments.of("blank name", VALID.replace("\"Winter Sale\"", "\"\""), "name"),
                Arguments.of("missing start date", VALID.replace("\"startDate\":\"2026-11-10\",", ""), "startDate"));
    }

    @ParameterizedTest(name = "400 for {0}")
    @MethodSource("invalidCampaignBodies")
    void createWithAnInvalidBodyReturns400AndCreatesNothing(String reason, String body, String expectedInDetail)
            throws Exception {
        mockMvc.perform(post("/api/offres/1/campaigns").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(problem(HttpStatus.BAD_REQUEST))
                .andExpect(detailContains(expectedInDetail));

        verifyNoInteractions(campaignService);
    }

    @Test
    void createWithAnUnknownPayoutModelReturns400() throws Exception {
        mockMvc.perform(post("/api/offres/1/campaigns").contentType(MediaType.APPLICATION_JSON)
                .content(VALID.replace("\"CPC\"", "\"CPM\"")))
                .andExpect(problem(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(campaignService);
    }

    @Test
    void createForAnUnknownOfferReturns404() throws Exception {
        when(campaignService.create(eq(99L), any(CampaignCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Offer not found: 99"));

        mockMvc.perform(post("/api/offres/99/campaigns").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(problem(HttpStatus.NOT_FOUND))
                .andExpect(detail("Offer not found: 99"));
    }

    @Test
    void createForAnInactiveAdvertiserReturns409() throws Exception {
        when(campaignService.create(eq(1L), any(CampaignCreateRequest.class)))
                .thenThrow(new ConflictException("Advertiser is not active"));

        mockMvc.perform(post("/api/offres/1/campaigns").contentType(MediaType.APPLICATION_JSON).content(VALID))
                .andExpect(problem(HttpStatus.CONFLICT));
    }

    // ---------- GET /api/campaigns ----------

    @Test
    void listWithoutFilterReturnsEveryCampaign() throws Exception {
        when(campaignService.findAll(null)).thenReturn(List.of(campaign(CampaignStatus.DRAFT), campaign(CampaignStatus.ACTIVE)));

        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(campaignService).findAll(isNull());
    }

    @Test
    void listWithAStatusFilterPassesItToTheService() throws Exception {
        when(campaignService.findAll(CampaignStatus.ACTIVE)).thenReturn(List.of(campaign(CampaignStatus.ACTIVE)));

        mockMvc.perform(get("/api/campaigns").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void listWithAnUnknownStatusReturns400() throws Exception {
        mockMvc.perform(get("/api/campaigns").param("status", "XYZ")).andExpect(problem(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(campaignService);
    }

    // ---------- PATCH /api/campaigns/{id}/status ----------

    @Test
    void changingTheStatusReturns204AndCallsTheService() throws Exception {
        mockMvc.perform(patch("/api/campaigns/5/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isNoContent());

        verify(campaignService).changeStatus(5L, CampaignStatus.ACTIVE);
    }

    @Test
    void changingTheStatusOfAnUnknownCampaignReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Campaign not found: 99"))
                .when(campaignService).changeStatus(99L, CampaignStatus.ACTIVE);

        mockMvc.perform(patch("/api/campaigns/99/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(problem(HttpStatus.NOT_FOUND));
    }

    @Test
    void aForbiddenTransitionReturns409() throws Exception {
        doThrow(new InvalidStatusTransitionException("Cannot go from DRAFT to PAUSED"))
                .when(campaignService).changeStatus(5L, CampaignStatus.PAUSED);

        mockMvc.perform(patch("/api/campaigns/5/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PAUSED\"}"))
                .andExpect(problem(HttpStatus.CONFLICT))
                .andExpect(detail("Cannot go from DRAFT to PAUSED"));
    }

    @Test
    void anUnknownStatusValueReturns400() throws Exception {
        mockMvc.perform(patch("/api/campaigns/5/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"XYZ\"}"))
                .andExpect(problem(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(campaignService);
    }

    @Test
    void aMissingStatusReturns400() throws Exception {
        mockMvc.perform(patch("/api/campaigns/5/status").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(problem(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(campaignService);
    }
}