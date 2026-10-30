package com.affiliateLink.offer.internal.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.affiliateLink.common.Niche;
import com.affiliateLink.offer.internal.dto.OfferCreateRequest;
import com.affiliateLink.offer.internal.dto.OfferResponse;
import com.affiliateLink.offer.internal.dto.OfferSummaryResponse;
import com.affiliateLink.offer.internal.dto.OfferUpdateRequest;
import com.affiliateLink.offer.internal.dto.PageResponse;
import com.affiliateLink.offer.internal.service.OfferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/offres")
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

    @GetMapping("/{id}")
    public OfferResponse getById(@PathVariable Long id) {
        return offerService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    public OfferResponse create(@Valid @RequestBody OfferCreateRequest request) {
        return offerService.create(request);
    }

    @PutMapping("/{id}") // 200
    public OfferResponse update(@PathVariable Long id,
            @Valid @RequestBody OfferUpdateRequest request) {
        return offerService.update(id, request);
    }
}
