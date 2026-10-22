package com.affiliateLink.profile.service;

import org.springframework.stereotype.Service;

import com.affiliateLink.common.DuplicateEmailException;
import com.affiliateLink.profile.dto.AdvertiserCreateRequest;
import com.affiliateLink.profile.entity.Advertiser;
import com.affiliateLink.profile.repository.AdvertiserRepository;
import com.affiliateLink.profile.repository.ProfileRepository;

@Service
public class AdvertiserService {

    private final AdvertiserRepository advertiserRepository;
    private final ProfileRepository profileRepository;

    public AdvertiserService(AdvertiserRepository advertiserRepository, ProfileRepository profileRepository) {
        this.advertiserRepository = advertiserRepository;
        this.profileRepository = profileRepository;
    }

    public Advertiser creaAdvertiser(AdvertiserCreateRequest request) {

        if (profileRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        Advertiser advertiser = new Advertiser(
                request.email(),
                request.nomEntreprise(),
                request.secteur(),
                request.siteWeb());
        return advertiserRepository.save(advertiser);
    }
}
