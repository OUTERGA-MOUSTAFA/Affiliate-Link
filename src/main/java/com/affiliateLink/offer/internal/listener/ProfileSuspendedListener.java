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
@Component 
 class ProfileSuspendedListener {

    private static final Logger log = LoggerFactory.getLogger(ProfileSuspendedListener.class);
    private final CampaignRepository campaignRepository;

    ProfileSuspendedListener(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @ApplicationModuleListener
    void on(ProfileSuspended event) {
        List<Campaign> active = campaignRepository /*
                                                    * hna kan go lih sir dir select offer o compaign dyale had
                                                    * advertiser li status dyale active
                                                    */
                .findByOfferAdvertiserIdAndStatus(event.profileId(), CampaignStatus.ACTIVE);

        active.forEach(Campaign::pause);// hna katgolih dez 3la kela compaign o dir liha pause
        log.info("Advertiser {} suspended: {} campaign(s) paused", event.profileId(), active.size());
    }
}
