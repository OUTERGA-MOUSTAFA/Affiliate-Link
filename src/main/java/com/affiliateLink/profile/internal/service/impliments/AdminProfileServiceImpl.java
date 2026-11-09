package com.affiliateLink.profile.internal.service.impliments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.ProfileSuspended;
import com.affiliateLink.profile.internal.entity.Profile;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.repository.ProfileRepository;
import com.affiliateLink.profile.internal.service.AdminProfileService;

@Service
public class AdminProfileServiceImpl implements AdminProfileService{

    private static final Logger log = LoggerFactory.getLogger(AdminProfileServiceImpl.class);

    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminProfileServiceImpl(ProfileRepository profileRepository, ApplicationEventPublisher eventPublisher) {
        this.profileRepository = profileRepository;
        this.eventPublisher = eventPublisher;
    }

    // Le listener de ProfileSuspended s’exécute après
    // la validation de la transaction du module profile,
    // dans sa propre transaction.
    @Override
    @Transactional // save event f event_publication
    public void updateStatus(Long id, ProfileStatus targetStatus) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profil introuvable avec l'id : " + id));

        ProfileStatus previous = profile.getStatus();
        try {
            switch (targetStatus) {
                case ACTIVE:
                    if (profile.getStatus() == ProfileStatus.PENDING) {
                        profile.activate();
                    } else if (profile.getStatus() == ProfileStatus.SUSPENDED) {
                        profile.reactivate();
                    } else {
                        throw new InvalidStatusTransitionException(
                                "Impossible de passer de " + profile.getStatus() + " à ACTIVE");
                    }
                    break;
                case SUSPENDED:
                    profile.suspend();
                    eventPublisher.publishEvent(new ProfileSuspended(profile.getId()));// hada hewa lbo9 li kay gol l
                                                                                       // offer
                                                                                       // o campaigin rah had advertiser
                                                                                       // id
                                                                                       // t suspenda dire khdemtkem
                    break;
                case PENDING:
                    throw new InvalidStatusTransitionException("Impossible de repasser un profil au statut PENDING");
                default:
                    throw new InvalidStatusTransitionException("Transition non supportée");
            }
        } catch (InvalidStatusTransitionException e) {
            log.warn("Status change refused for profile {}: {} -> {}", id, previous, targetStatus);
            throw e; // خاص يتعاود يترمى، وإلا الـ 409 كتضيع
        }
        profileRepository.save(profile);
        log.info("Profile {} status changed: {} -> {}", id, previous, targetStatus);
    }


}
