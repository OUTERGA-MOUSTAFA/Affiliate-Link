package com.affiliateLink.profile.internal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.affiliateLink.profile.AdvertiserInfo;
import com.affiliateLink.profile.ProfileApi;
import com.affiliateLink.profile.internal.entity.Profile;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.repository.AdvertiserRepository;
import com.affiliateLink.profile.internal.repository.PublisherRepository;

@Service
@Transactional(readOnly = true)
public class ProfileApiImpl implements ProfileApi {

    private final AdvertiserRepository advertiserRepository;
    private final PublisherRepository publisherRepository;
    public ProfileApiImpl(AdvertiserRepository advertiserRepository, PublisherRepository publisherRepository) {
        this.advertiserRepository = advertiserRepository;
        this.publisherRepository = publisherRepository;
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

    @Override
    public List<String> getActivePublisherEmails() {

        return publisherRepository.findByStatus(ProfileStatus.ACTIVE).stream().map(Profile::getEmail).toList();
    }
}