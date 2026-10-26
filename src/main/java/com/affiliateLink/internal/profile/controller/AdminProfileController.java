package com.affiliateLink.internal.profile.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.internal.profile.dto.StatusUpdateRequest;
import com.affiliateLink.internal.profile.entity.Profile;
import com.affiliateLink.internal.profile.entity.ProfileStatus;
import com.affiliateLink.internal.profile.repository.ProfileRepository;
import com.affiliateLink.internal.profile.service.AdminProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminProfileController {
    // Profile profile = profileRepository.findById(id)
    // .orElseThrow(() -> new ResourceNotFoundException("Profil introuvable"));

    // switch(requestedStatus)
    // {
    // case ACTIVE -> {
    // if (profile.getStatus() == ProfileStatus.PENDING) profile.activate();
    // else profile.reactivate(); // vient de SUSPENDED
    // }
    // case SUSPENDED -> profile.suspend();
    // default -> throw new InvalidStatusTransitionException("Transition non
    // supportée");

    // private final ProfileRepository profileRepository;


    // public AdminProfileController(ProfileRepository profileRepository) {
    //     this.profileRepository = profileRepository;
    // }

    private final AdminProfileService adminProfileService;
    public AdminProfileController(AdminProfileService adminProfileService){
        this.adminProfileService = adminProfileService;
    }
    @PatchMapping("/profiles/{id}/status")
    // public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
    //     Profile profile = profileRepository.findById(id)
    //             .orElseThrow(() -> new ResourceNotFoundException("Profil introuvable"));

        // switch (request.status()) {
        //     case ACTIVE:
        //         if (profile.getStatus() == ProfileStatus.PENDING) {
        //             profile.activate();
        //         } else {
        //             profile.reactivate();
        //         }
        //         break;
        //     case SUSPENDED:
        //         profile.suspend();
        //         break;

        //     default:
        //         throw new InvalidStatusTransitionException("Transition non supportée");
        // }
        
    //     return ResponseEntity.noContent().build();
    // }
    public ResponseEntity<Void>updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request){
        adminProfileService.updateStatus(id, request.status());
        return ResponseEntity.noContent().build();
    }



}
