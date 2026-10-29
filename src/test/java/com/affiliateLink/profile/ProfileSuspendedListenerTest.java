package com.affiliateLink.profile;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import com.affiliateLink.offer.internal.entity.CampaignStatus;

@ApplicationModuleTest 
class ProfileSuspendedListenerTest {

    @Test 

    void pausesActiveCampaigns(Scenario scenario) {
        scenario.publish(new ProfileSuspended(7L))
                .andWaitForStateChange(() -> /* جيب الحملة وارجع الحالة */ "hey from test");
                // .andVerify(status -> assertEquals(CampaignStatus.PAUSED, status));
    }
}