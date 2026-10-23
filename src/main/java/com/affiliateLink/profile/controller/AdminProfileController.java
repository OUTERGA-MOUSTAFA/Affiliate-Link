package com.affiliateLink.profile.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.entity.ProfileStatus;

@RestController 
@RequestMapping ("/api/admin")
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

    private final ProfileRepository profileRepository;
    public AdminProfileController(ProfileRepository profileRepository){
        this.profileRepository = profileRepository;
    }


    @PatchMapping ("/profiles/{id}/status")
    public {
        return "Patch request Mostafa";
    }
}
