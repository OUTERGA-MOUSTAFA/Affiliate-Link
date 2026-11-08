package com.affiliateLink.offer;

import java.util.Optional;

public interface OfferApi {
    // getCampaign(id)
    Optional<CampaignInfo> getCampaign(Long campaignId);
}
