package com.affiliateLink.offer.internal.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.repository.CampaignRepository;
import com.affiliateLink.profile.ProfileSuspended; 

public class ProfileSuspendedListener {

    private static final Logger log = LoggerFactory.getLogger(ProfileSuspendedListener.class);

    private final CampaignRepository campaignRepository;

    ProfileSuspendedListener(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @ApplicationModuleListener 
    void on(ProfileSuspended event) {
        List<Campaign> active = campaignRepository
                .findByOfferAdvertiserIdAndStatus(event.profileId(), CampaignStatus.ACTIVE);

        active.forEach(Campaign::pause);   // الـ dirty checking كيكتب التعديلات
        log.info("Advertiser {} suspended: {} campaign(s) paused", event.profileId(), active.size());
    }
}
