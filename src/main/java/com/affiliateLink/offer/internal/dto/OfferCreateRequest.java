package com.affiliateLink.offer.internal.dto;

import java.util.Set;

import com.affiliateLink.common.Niche;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OfferCreateRequest(
        @NotNull Long advertiserId,
        @NotBlank @Size(max = 100) String title,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 500) @Pattern(regexp = "^https?://.+", message = "must be a valid http(s) URL") String landingUrl,
        @NotNull Niche niche,
        Set<@NotBlank @Size(max = 40) String> tags) {

    public OfferCreateRequest {
        tags = (tags == null) ? Set.of() : tags; // ما نبقاوش نتعاملو مع null
    }
}
