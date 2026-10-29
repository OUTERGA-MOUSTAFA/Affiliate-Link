package com.affiliateLink.offer.internal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.entity.PayoutModel;

public record CampaignResponse(
        Long id,
        Long offerId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budget,
        PayoutModel payoutModel,
        BigDecimal payoutValue,
        CampaignStatus status) {
// consulte une offre avec ses campagnes
}
