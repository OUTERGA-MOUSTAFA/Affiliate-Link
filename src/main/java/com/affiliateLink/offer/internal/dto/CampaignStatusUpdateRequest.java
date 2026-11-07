package com.affiliateLink.offer.internal.dto;

import com.affiliateLink.offer.internal.entity.CampaignStatus;

import jakarta.validation.constraints.NotNull;
// request to update status of campain
// { "status": "ACTIVE" }
//PATCH /api/campaigns/{id}/status
public record CampaignStatusUpdateRequest(@NotNull CampaignStatus status) {
}