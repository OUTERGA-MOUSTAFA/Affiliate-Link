package com.affiliateLink.offer.internal.dto;

import java.time.Instant;
import java.util.Set;

public record OfferSummaryResponse(
        Long id,
        Long advertiserId,
        String title,
        String description,
        String landingUrl,
        String niche,
        Instant createdAt,
        Set<String> tags) {

           
}
