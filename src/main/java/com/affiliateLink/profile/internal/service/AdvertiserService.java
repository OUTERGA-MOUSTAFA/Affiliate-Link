package com.affiliateLink.profile.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.affiliateLink.common.DuplicateEmailException;
import com.affiliateLink.profile.internal.dto.AdvertiserCreateRequest;
import com.affiliateLink.profile.internal.entity.Advertiser;
import com.affiliateLink.profile.internal.repository.AdvertiserRepository;
import com.affiliateLink.profile.internal.repository.ProfileRepository;

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
           throw new DuplicateEmailException("Un compte existe déjà avec l'email : " + request.email());
        }

        Advertiser advertiser = new Advertiser(
                request.email(),
                request.nomEntreprise(),
                request.secteur(),
                request.siteWeb());
        return advertiserRepository.save(advertiser);
    }

    public List<Advertiser> findAll(){
        return advertiserRepository.findAll();
    }

    public Advertiser findById(Long id){
        
        return advertiserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertiser introuvable avec l'id : " + id));
    }
}
