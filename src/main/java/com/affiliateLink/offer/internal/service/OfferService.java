package com.affiliateLink.offer.internal.service;

import com.affiliateLink.common.Niche;
import com.affiliateLink.offer.internal.dto.OfferCreateRequest;
import com.affiliateLink.offer.internal.dto.OfferResponse;
import com.affiliateLink.offer.internal.dto.OfferSummaryResponse;
import com.affiliateLink.offer.internal.dto.OfferUpdateRequest;
import com.affiliateLink.offer.internal.dto.PageResponse;

import org.springframework.data.domain.Pageable;

public interface OfferService {

    OfferResponse create(OfferCreateRequest request);

    OfferResponse update(Long offerId, OfferUpdateRequest request);

    OfferResponse getById(Long id);

    PageResponse<OfferSummaryResponse> search(Niche niche, Long advertiserId, String tag,
            String keyword, Pageable pageable);
}