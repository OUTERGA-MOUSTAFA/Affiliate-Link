package com.affiliateLink.offer.internal.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.affiliateLink.common.Niche;
import com.affiliateLink.offer.internal.dto.OfferSummaryResponse;
import com.affiliateLink.offer.internal.dto.PageResponse;
import com.affiliateLink.offer.internal.service.OfferService;

public class OfferController {
    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/api/offres")
    public PageResponse<OfferSummaryResponse> search(
            @RequestParam(required = false) Niche niche,
            @RequestParam(required = false) Long advertiserId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return offerService.search(niche, advertiserId, tag, keyword, pageable);
    }
}
