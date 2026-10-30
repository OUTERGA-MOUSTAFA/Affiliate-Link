package com.affiliateLink.offer.internal.dto;

import java.time.Instant;
import java.util.Set;

import com.affiliateLink.common.Niche;

public record OfferSummaryResponse(
                Long id,
                Long advertiserId,
                String title,
                String description,
                String landingUrl,
                Niche niche,
                Instant createdAt,
                Set<String> tags) {

}
