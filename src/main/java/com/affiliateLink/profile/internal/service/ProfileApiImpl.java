package com.affiliateLink.profile.internal.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.affiliateLink.profile.AdvertiserInfo;
import com.affiliateLink.profile.ProfileApi;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.repository.AdvertiserRepository;

@Service
@Transactional(readOnly = true)
public class ProfileApiImpl implements ProfileApi {

    private final AdvertiserRepository advertiserRepository;

    public ProfileApiImpl(AdvertiserRepository advertiserRepository) {
        this.advertiserRepository = advertiserRepository;
    }

    @Override
    public Optional<AdvertiserInfo> getAdvertiser(Long id) {
        return advertiserRepository.findById(id)
                .map(adv -> new AdvertiserInfo(adv.getId(), adv.getEmail(),
                        adv.getNomEntreprise(), adv.getSecteur(), adv.getSiteWeb(),
                        adv.getStatus() == ProfileStatus.ACTIVE));
    }

    @Override
    public boolean isActive(Long id) {
        return advertiserRepository.findById(id)
                .map(adv -> adv.getStatus() == ProfileStatus.ACTIVE)
                .orElse(false);
    }
}