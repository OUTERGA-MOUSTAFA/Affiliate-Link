package com.affiliateLink.offer.internal.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.affiliateLink.offer.CampaignInfo;
import com.affiliateLink.offer.OfferApi;
import com.affiliateLink.offer.internal.repository.CampaignRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OfferApiImpl implements OfferApi {

    private final CampaignRepository campaignRepository;

    public OfferApiImpl(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Override
    public Optional<CampaignInfo> getCampaign(Long campaignId) {
        return campaignRepository.findById(campaignId).map(campaigin -> new CampaignInfo(campaigin.getId(),
                                                                                        campaigin.getOffer().getId(),
                                                                                        campaigin.getOffer().getAdvertiserId(), // الـ offer lazy: خدام داخل الـ transaction
                                                                                        campaigin.getName(),
                                                                                        campaigin.getStartDate(),
                                                                                        campaigin.getEndDate(),
                                                                                        campaigin.getBudget(),
                                                                                        campaigin.getPayoutModel().name(),
                                                                                        campaigin.getPayoutValue(),
                                                                                        campaigin.getStatus().name()));
    }
}