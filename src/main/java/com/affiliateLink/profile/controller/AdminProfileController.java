package com.affiliateLink.profile.controller;

import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.entity.ProfileStatus;

public class AdminProfileController {
    // Profile profile = profileRepository.findById(id)
    // .orElseThrow(() -> new ResourceNotFoundException("Profil introuvable"));

    // switch(requestedStatus)
    // {
    // case ACTIVE -> {
    //     if (profile.getStatus() == ProfileStatus.PENDING) profile.activate();
    //     else profile.reactivate();  // vient de SUSPENDED
    // }
    // case SUSPENDED -> profile.suspend();
    // default -> throw new InvalidStatusTransitionException("Transition non supportée");

}
