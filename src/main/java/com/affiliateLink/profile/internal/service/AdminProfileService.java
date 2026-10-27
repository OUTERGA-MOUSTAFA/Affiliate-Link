package com.affiliateLink.profile.internal.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.ProfileSuspended;
import com.affiliateLink.profile.internal.entity.Profile;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.repository.ProfileRepository;

@Service 
public class AdminProfileService {
    
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;
    public AdminProfileService(ProfileRepository profileRepository, ApplicationEventPublisher eventPublisher){
        this.profileRepository = profileRepository;
        this.eventPublisher = eventPublisher;
    }

    // Le listener de ProfileSuspended s’exécute après 
    // la validation de la transaction du module profile, 
    // dans sa propre transaction.
    @Transactional 
    public void updateStatus(Long id, ProfileStatus targetStatus){
        Profile profile = profileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Profil introuvable avec l'id : " + id));

        switch (targetStatus) {
            case ACTIVE :
                 if (profile.getStatus() == ProfileStatus.PENDING) {
                    profile.activate();
                } else if (profile.getStatus() == ProfileStatus.SUSPENDED) {
                    profile.reactivate();
                } else {
                    throw new InvalidStatusTransitionException(
                        "Impossible de passer de " + profile.getStatus() + " à ACTIVE"
                    );
                }
                break;
            case SUSPENDED:
                profile.suspend();
                eventPublisher.publishEvent(new ProfileSuspended(profile.getId()));
                break;
            case PENDING:
                throw new InvalidStatusTransitionException("Impossible de repasser un profil au statut PENDING");
            default:
                throw new InvalidStatusTransitionException("Transition non supportée");
        }
        profileRepository.save(profile);
    }
}
