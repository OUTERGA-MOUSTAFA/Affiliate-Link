package com.affiliateLink.profile;

import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.profile.entity.Niche;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.profile.entity.Publisher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTransitionTest {

    private Publisher newPendingPublisher() {
        return new Publisher("test@mail.com", "Ahmed", "Ben", Niche.TECH, "bio", "Maroc");
    }

    @Test
    void nouveauProfilDoitEtrePending() {
        Publisher p = newPendingPublisher();
        assertEquals(ProfileStatus.PENDING, p.getStatus());
    }

    @Test
    void activatePermetDePasserDePendingAActive() {
        Publisher p = newPendingPublisher();
        p.activate();
        assertEquals(ProfileStatus.ACTIVE, p.getStatus());
    }

    @Test
    void suspendPermetDePasserDActiveASuspended() {
        Publisher p = newPendingPublisher();
        p.activate();
        p.suspend();
        assertEquals(ProfileStatus.SUSPENDED, p.getStatus());
    }

    @Test
    void reactivatePermetDePasserDeSuspendedAActive() {
        Publisher p = newPendingPublisher();
        p.activate();
        p.suspend();
        p.reactivate();
        assertEquals(ProfileStatus.ACTIVE, p.getStatus());
    }

    @Test
    void activateDepuisActiveDoitEchouer() {
        Publisher p = newPendingPublisher();
        p.activate();
        assertThrows(InvalidStatusTransitionException.class, p::activate);
    }

    @Test
    void suspendDepuisPendingDoitEchouer() {
        Publisher p = newPendingPublisher();
        assertThrows(InvalidStatusTransitionException.class, p::suspend);
    }

    @Test
    void reactivateDepuisPendingDoitEchouer() {
        Publisher p = newPendingPublisher();
        assertThrows(InvalidStatusTransitionException.class, p::reactivate);
    }

    @Test
    void activateDepuisSuspendedDoitEchouer() {
        Publisher p = newPendingPublisher();
        p.activate();
        p.suspend();
        // SUSPENDED -> ACTIVE ماشي عبر activate(), عبر reactivate() فقط
        assertThrows(InvalidStatusTransitionException.class, p::activate);
    }
}