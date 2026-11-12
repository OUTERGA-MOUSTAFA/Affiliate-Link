package com.affiliateLink.offer.internal.controller;

import static com.affiliateLink.ProblemDetailMatchers.detail;
import static com.affiliateLink.ProblemDetailMatchers.detailContains;
import static com.affiliateLink.ProblemDetailMatchers.problem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.affiliateLink.common.ConflictException;
import com.affiliateLink.common.Niche;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.internal.dto.CampaignResponse;
import com.affiliateLink.offer.internal.dto.OfferCreateRequest;
import com.affiliateLink.offer.internal.dto.OfferResponse;
import com.affiliateLink.offer.internal.dto.OfferSummaryResponse;
import com.affiliateLink.offer.internal.dto.OfferUpdateRequest;
import com.affiliateLink.offer.internal.dto.PageResponse;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.entity.PayoutModel;
import com.affiliateLink.offer.internal.service.OfferService;

@WebMvcTest(OfferController.class)
@EnableSpringDataWebSupport   // garantit la résolution de Pageable (page, size, sort)
class OfferControllerIT {

    private static final String VALID_CREATE = """
            {"advertiserId":1,"title":"Pro VPN","description":"Fast VPN","landingUrl":"https://vpn.example.com",
             "niche":"TECH","tags":["VPN","security"]}""";

    private static final String VALID_UPDATE = """
            {"title":"Pro VPN 2","description":"New","landingUrl":"https://vpn.example.com",
             "niche":"TECH","tags":["vpn"]}""";

    @Autowired MockMvc mockMvc;
    @MockitoBean OfferService offerService;

    private OfferResponse offerResponse(List<CampaignResponse> campaigns) {
        return new OfferResponse(1L, 1L, "Pro VPN", "Fast VPN", "https://vpn.example.com", Niche.TECH,
                Instant.parse("2026-11-10T10:00:00Z"), Set.of("vpn"), campaigns);
    }

    // ---------- POST /api/offres ----------

    @Test
    void createReturns201WithTheCreatedOffer() throws Exception {
        when(offerService.create(any(OfferCreateRequest.class))).thenReturn(offerResponse(List.of()));

        mockMvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.niche").value("TECH"))
                .andExpect(jsonPath("$.tags[0]").value("vpn"));
    }

    static Stream<Arguments> invalidCreateBodies() {
        return Stream.of(
                Arguments.of("blank title", VALID_CREATE.replace("\"Pro VPN\"", "\"  \"")),
                Arguments.of("landing url without http", VALID_CREATE.replace("https://vpn.example.com", "vpn.example.com")),
                Arguments.of("lowercase niche", VALID_CREATE.replace("\"TECH\"", "\"tech\"")),
                Arguments.of("unknown niche", VALID_CREATE.replace("\"TECH\"", "\"CRYPTO\"")),
                Arguments.of("missing advertiserId", VALID_CREATE.replace("\"advertiserId\":1,", "")),
                Arguments.of("blank tag", VALID_CREATE.replace("\"security\"", "\" \"")));
    }

    @ParameterizedTest(name = "400 for {0}")
    @MethodSource("invalidCreateBodies")
    void createWithAnInvalidBodyReturns400AndCreatesNothing(String reason, String body) throws Exception {
        mockMvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(problem(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(offerService);
    }

    @Test
    void createForAnUnknownAdvertiserReturns404() throws Exception {
        when(offerService.create(any(OfferCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Advertiser not found: 1"));

        mockMvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(problem(HttpStatus.NOT_FOUND))
                .andExpect(detail("Advertiser not found: 1"));
    }

    @Test
    void createForAnInactiveAdvertiserReturns409() throws Exception {
        when(offerService.create(any(OfferCreateRequest.class)))
                .thenThrow(new ConflictException("Advertiser is not active"));

        mockMvc.perform(post("/api/offres").contentType(MediaType.APPLICATION_JSON).content(VALID_CREATE))
                .andExpect(problem(HttpStatus.CONFLICT))
                .andExpect(detail("Advertiser is not active"));
    }

    // ---------- PUT /api/offres/{id} ----------

    @Test
    void updateReturns200WithTheUpdatedOffer() throws Exception {
        when(offerService.update(eq(1L), any(OfferUpdateRequest.class))).thenReturn(offerResponse(List.of()));

        mockMvc.perform(put("/api/offres/1").contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Pro VPN"));
    }

    @Test
    void updateWithABlankTitleReturns400() throws Exception {
        mockMvc.perform(put("/api/offres/1").contentType(MediaType.APPLICATION_JSON)
                .content(VALID_UPDATE.replace("\"Pro VPN 2\"", "\"\"")))
                .andExpect(problem(HttpStatus.BAD_REQUEST))
                .andExpect(detailContains("title"));

        verifyNoInteractions(offerService);
    }

    @Test
    void updateOfAnUnknownOfferReturns404() throws Exception {
        when(offerService.update(eq(99L), any(OfferUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Offer not found: 99"));

        mockMvc.perform(put("/api/offres/99").contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE))
                .andExpect(problem(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateWhenTheAdvertiserIsNoLongerActiveReturns409() throws Exception {
        when(offerService.update(eq(1L), any(OfferUpdateRequest.class)))
                .thenThrow(new ConflictException("Advertiser is not active"));

        mockMvc.perform(put("/api/offres/1").contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE))
                .andExpect(problem(HttpStatus.CONFLICT));
    }

    // ---------- GET /api/offres/{id} ----------

    @Test
    void getByIdReturns200WithTheCampaigns() throws Exception {
        CampaignResponse campaign = new CampaignResponse(5L, 1L, "Winter", LocalDate.parse("2026-11-10"),
                LocalDate.parse("2027-01-31"), new BigDecimal("1000.00"), PayoutModel.CPC,
                new BigDecimal("0.50"), CampaignStatus.ACTIVE);
        when(offerService.getById(1L)).thenReturn(offerResponse(List.of(campaign)));

        mockMvc.perform(get("/api/offres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns.length()").value(1))
                .andExpect(jsonPath("$.campaigns[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.campaigns[0].payoutModel").value("CPC"));
    }

    @Test
    void getByIdOfAnUnknownOfferReturns404() throws Exception {
        when(offerService.getById(99L)).thenThrow(new ResourceNotFoundException("Offer not found: 99"));

        mockMvc.perform(get("/api/offres/99"))
                .andExpect(problem(HttpStatus.NOT_FOUND))
                .andExpect(detail("Offer not found: 99"));
    }

    // ---------- GET /api/offres (recherche) ----------

    private PageResponse<OfferSummaryResponse> onePage() {
        OfferSummaryResponse summary = new OfferSummaryResponse(1L, 7L, "Pro VPN", "Fast VPN",
                "https://vpn.example.com", Niche.TECH, Instant.parse("2026-11-10T10:00:00Z"), Set.of("vpn"));
        return new PageResponse<>(List.of(summary), 1, 5, 6, 2);
    }

    @Test
    void searchPassesEveryCriterionAndThePaginationToTheService() throws Exception {
        when(offerService.search(any(), any(), any(), any(), any(Pageable.class))).thenReturn(onePage());

        mockMvc.perform(get("/api/offres")
                .param("niche", "TECH").param("advertiserId", "7").param("tag", "vpn").param("keyword", "pro")
                .param("page", "1").param("size", "5").param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Pro VPN"))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(offerService).search(eq(Niche.TECH), eq(7L), eq("vpn"), eq("pro"), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageable.getValue().getSort().getOrderFor("title").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void searchWithoutAnyCriterionStillReturns200() throws Exception {
        when(offerService.search(any(), any(), any(), any(), any(Pageable.class))).thenReturn(onePage());

        mockMvc.perform(get("/api/offres")).andExpect(status().isOk());

        verify(offerService).search(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void searchWithAnUnknownNicheReturns400() throws Exception {
        mockMvc.perform(get("/api/offres").param("niche", "XYZ")).andExpect(problem(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(offerService);
    }

    @Test
    void searchWithAnUnsupportedSortFieldReturns400() throws Exception {
        when(offerService.search(any(), any(), any(), any(), any(Pageable.class)))
                .thenThrow(new IllegalArgumentException("Unsupported sort field: password"));

        mockMvc.perform(get("/api/offres").param("sort", "password,asc"))
                .andExpect(problem(HttpStatus.BAD_REQUEST))
                .andExpect(detailContains("password"));
    }
}