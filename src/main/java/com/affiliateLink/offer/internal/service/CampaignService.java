package com.affiliateLink.offer.internal.service;

import java.util.List;

import com.affiliateLink.offer.internal.dto.CampaignCreateRequest;
import com.affiliateLink.offer.internal.dto.CampaignResponse;
import com.affiliateLink.offer.internal.entity.CampaignStatus;

public interface CampaignService {
    CampaignResponse create(Long offerId, CampaignCreateRequest request);
    
    List<CampaignResponse> findAll(CampaignStatus status);

    void changeStatus(Long campaignId, CampaignStatus targeStatus);
}
