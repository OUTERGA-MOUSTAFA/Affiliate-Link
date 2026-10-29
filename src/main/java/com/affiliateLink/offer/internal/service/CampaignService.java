package com.affiliateLink.offer.internal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.repository.CampaignRepository;

@Service
@Transactional(readOnly = true) // les Queries li ghadare fhad class ghir dyale select safe.
// why: Performance Optimization
public class CampaignService {
    private static final Logger log = LoggerFactory.getLogger(CampaignService.class);// had ghir bash nhet logs f
                                                                                     // logs/affiliatelink.log

    private final CampaignRepository campaignRepository;

    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Transactional 
    public void changeStatus(Long campingnId, CampaignStatus targeStatus){

        Campaign campaign = campaignRepository.findById(campingnId).orElseThrow(
                ()-> new ResourceNotFoundException("Campaign not found: " + campingnId)
        );

        switch (targeStatus) {
            case ACTIVE-> activate(campaign);
            case PAUSED -> pause();
            case ENDED -> end();        
            default: new InvalidStatusTransitionException("A campaign cannot go back to " + targeStatus);
        }
        log.info("Campaign {} is now {}", campingnId, campaign.getStatus());
    }


    private void activate(Campaign campaign) {
        campaignRepository.findByOfferIdAndStatus(campaign.getOffer().getId(), CampaignStatus.ACTIVE)
                .filter(current -> !current.getId().equals(campaign.getId()))
                .ifPresent(current -> {
                    current.pause();
                    campaignRepository.saveAndFlush(current);   // خاصها تتكتب قبل الجديدة
                    log.info("Campaign {} paused: replaced by {}", current.getId(), campaign.getId());
                });

        campaign.activate();   // كترمي exception إلا الانتقال ممنوع أو التاريخ فات
    }
}
