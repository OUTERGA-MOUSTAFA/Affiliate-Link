package com.affiliateLink.offer.internal.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.affiliateLink.offer.internal.service.NotificationSender;
import com.affiliateLink.profile.AdvertiserInfo;
import com.affiliateLink.profile.ProfileApi;
import com.affiliateLink.profile.ProfileSuspended;

@Component 
class PublisherNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(PublisherNotificationListener.class);

    private final ProfileApi profileApi;
    private final NotificationSender notificationSender;

    PublisherNotificationListener(ProfileApi profileApi, NotificationSender notificationSender) {
        this.profileApi = profileApi;
        this.notificationSender = notificationSender;
    }

    @ApplicationModuleListener 
    void on(ProfileSuspended event) {
        // الـ profile اللي توقف ممكن يكون publisher، ماشي غير advertiser
        String company = profileApi.getAdvertiser(event.profileId())
                .map(AdvertiserInfo::companyName)
                .orElse(null);

        if (company == null) {
            log.debug("Profile {} is not an advertiser: no publisher notification", event.profileId());
            return;
        }

        List<String> emails = profileApi.getActivePublisherEmails();
        if (emails.isEmpty()) {
            log.info("No active publisher to notify for advertiser {}", event.profileId());
            return;
        }

        notificationSender.send(emails,
                "Stop sharing links of " + company,
                "The advertiser " + company + " has been suspended. "
                        + "Please stop sharing its links on your social networks.");
    }
}