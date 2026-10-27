package com.affiliateLink.profile.internal;

import com.affiliateLink.profile.ProfileSuspended;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class DebugListener {

    // test event 
    @ApplicationModuleListener
    public void on(ProfileSuspended event) {
        System.out.println(">>> ProfileSuspended reçu pour profileId=" + event.profileId());
    }
}