package com.affiliateLink.internal.profile.service;

import org.springframework.stereotype.Service;

import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.internal.profile.entity.Profile;
import com.affiliateLink.internal.profile.entity.ProfileStatus;
import com.affiliateLink.internal.profile.repository.ProfileRepository;

@Service 
public class AdminProfileService {
    
    private final ProfileRepository profileRepository;

    public AdminProfileService(ProfileRepository profileRepository){
        this.profileRepository = profileRepository;
    }

    public void updateStatus(Long id, ProfileStatus targetStatus){
        Profile profile = profileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Profil introuvable avec l'id : " + id));

        switch (targetStatus) {
            case ACTIVE :
                 if (profile.getStatus() == ProfileStatus.PENDING) {
                    profile.activate();
                } else {
                    profile.reactivate();
                }
                break;
            case SUSPENDED:
                profile.suspend();
                break;
            case PENDING:
                throw new InvalidStatusTransitionException("Impossible de repasser un profil au statut PENDING");
            default:
                throw new InvalidStatusTransitionException("Transition non supportée");
        }
        profileRepository.save(profile);
    }
}
