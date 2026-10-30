package com.affiliateLink.offer.internal.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.affiliateLink.common.Niche;

public record OfferResponse(
                Long id,
                Long advertiserId,
                String title,
                String description,
                String landingUrl,
                Niche niche,
                Instant createdAt,
                Set<String> tags,
                List<CampaignResponse> campaigns) {

        // la recherche list offre dyale un advertiser
}
