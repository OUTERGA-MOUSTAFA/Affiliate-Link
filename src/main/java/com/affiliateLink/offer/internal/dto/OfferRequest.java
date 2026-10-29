package com.affiliateLink.offer.internal.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OfferRequest(
        @NotNull Long advertiserId,
        @NotBlank @Size(max = 100) String title,
        String description,
        @NotBlank @Size(max = 500) @Pattern(regexp = "^https?://.+", message = "must be a valid http(s) URL") String landingUrl,
        @NotBlank @Pattern(regexp = "BEAUTY|TECH|GAMING|TRAVEL|LIFESTYLE|FOOD|SPORT", message = "unknown niche") String niche,
        Set<@NotBlank @Size(max = 40) String> tags) {

    public OfferRequest {
        tags = (tags == null) ? Set.of() : tags; // ما نبقاوش نتعاملو مع null
    }
}
