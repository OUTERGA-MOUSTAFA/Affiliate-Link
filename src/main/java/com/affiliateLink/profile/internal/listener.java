package com.affiliateLink.profile.internal;

import org.springframework.modulith.events.ApplicationModuleListener;

import com.affiliateLink.profile.ProfileSuspended;

public class listener {
    
    
    // test event 
    @ApplicationModuleListener // kaykhdem after commit dyale @Transactional
    public void on(ProfileSuspended event) {
        System.out.println(">>> ProfileSuspended reçu pour profileId=" + event.profileId());
    }
    
}
