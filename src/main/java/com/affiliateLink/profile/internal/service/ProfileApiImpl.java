package com.affiliateLink.profile.internal.service;

import java.util.Optional;
import org.springframework.stereotype.Service;

import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.AdvertiserInfo;
import com.affiliateLink.profile.ProfileApi;
import com.affiliateLink.profile.ProfileSuspended;
import com.affiliateLink.profile.internal.entity.Profile;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.repository.AdvertiserRepository;

import jakarta.transaction.Transactional;

@Service
public class ProfileApiImpl implements ProfileApi {
    private final AdvertiserRepository advertiserRepository;

    public ProfileApiImpl(AdvertiserRepository advertiserRepository) {
        this.advertiserRepository = advertiserRepository;
    }

    @Override
    public Optional<AdvertiserInfo> getAdvertiser(Long id) {
        return advertiserRepository.findById(id).map(adv -> new AdvertiserInfo(adv.getId(), adv.getEmail(),
                adv.getNomEntreprise(), adv.getStatus() == ProfileStatus.ACTIVE));
    }

    @Transactional
    public void suspend(Long profileId){
        Profile profile = advertiserRepository.findById(profileId).orElseThrow(() -> new ResourceNotFoundException("Profil introuvable avec l'id : " + profileId));
        profile.suspend();
        // events.publishEvent(new ProfileSuspended(profile.getId()));

    }
}
