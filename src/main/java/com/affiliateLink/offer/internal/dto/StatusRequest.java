package com.affiliateLink.offer.internal.dto;

import com.affiliateLink.offer.internal.entity.CampaignStatus;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull  CampaignStatus status) {}