package com.affiliateLink.offer.internal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.affiliateLink.offer.internal.entity.PayoutModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CampaignRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @Positive BigDecimal budget,
        @NotNull PayoutModel payoutModel,
        @NotNull @Positive BigDecimal payoutValue) {

}
