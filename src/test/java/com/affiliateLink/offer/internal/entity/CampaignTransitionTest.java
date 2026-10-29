package com.affiliateLink.offer.internal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;


class CampaignTransitionTest {

  
    // helper: حملة صالحة وكتتخلق DRAFT
    // private Campaign newCampaign() {
    //     return new Campaign("Summer", LocalDate.now(), LocalDate.now().plusDays(10),
    //             BigDecimal.valueOf(1000), PayoutModel.CPC, BigDecimal.valueOf(2));
    // }

    // @Test
    // void newCampaignIsDraft() {
    //     assertEquals(CampaignStatus.DRAFT, newCampaign().getStatus());
    // }

    // @Test
    // void draftCanBeActivated() {
    //     Campaign c = newCampaign();
    //     c.activate();
    //     assertEquals(CampaignStatus.ACTIVE, c.getStatus());
    // }

    // @Test
    // void activeCanBePausedThenReactivated() {
    //     Campaign c = newCampaign();
    //     c.activate();
    //     c.pause();
    //     assertEquals(CampaignStatus.PAUSED, c.getStatus());
    //     c.activate();
    //     assertEquals(CampaignStatus.ACTIVE, c.getStatus());
    // }

    // @Test
    // void activeCanBeEnded() {
    //     Campaign c = newCampaign();
    //     c.activate();
    //     c.end();
    //     assertEquals(CampaignStatus.ENDED, c.getStatus());
    // }

    // @Test
    // void draftCannotBePaused() {
    //     assertThrows(InvalidStatusTransitionException.class, () -> newCampaign().pause());
    // }

    // @Test
    // void draftCannotBeEnded() {
    //     assertThrows(InvalidStatusTransitionException.class, () -> newCampaign().end());
    // }

    // @Test
    // void endedCampaignCannotChangeAnymore() {
    //     Campaign c = newCampaign();
    //     c.activate();
    //     c.end();
    //     assertThrows(InvalidStatusTransitionException.class, c::activate);
    //     assertThrows(InvalidStatusTransitionException.class, c::pause);
    //     assertThrows(InvalidStatusTransitionException.class, c::end);
    // }

    // @Test
    // void campaignWithPastEndDateCannotBeActivated() {
    //     Campaign expired = new Campaign("Old", LocalDate.now().minusDays(10), LocalDate.now().minusDays(1),
    //             BigDecimal.valueOf(1000), PayoutModel.CPC, BigDecimal.valueOf(2));
    //     assertThrows(InvalidStatusTransitionException.class, expired::activate);
    // }
}