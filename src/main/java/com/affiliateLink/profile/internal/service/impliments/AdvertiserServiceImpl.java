package com.affiliateLink.profile.internal.service.impliments;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.affiliateLink.common.DuplicateEmailException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.internal.dto.AdvertiserCreateRequest;
import com.affiliateLink.profile.internal.entity.Advertiser;
import com.affiliateLink.profile.internal.repository.AdvertiserRepository;
import com.affiliateLink.profile.internal.repository.ProfileRepository;
import com.affiliateLink.profile.internal.service.AdvertiserService;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdvertiserServiceImpl implements AdvertiserService {

    private static final Logger log = LoggerFactory.getLogger(AdvertiserServiceImpl.class);

    private final AdvertiserRepository advertiserRepository;
    private final ProfileRepository profileRepository;

    public AdvertiserServiceImpl(AdvertiserRepository advertiserRepository, ProfileRepository profileRepository) {
        this.advertiserRepository = advertiserRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public Advertiser creaAdvertiser(AdvertiserCreateRequest request) {

        if (profileRepository.existsByEmail(request.email())) {
            log.warn("Advertiser creation refused: email already used");
            throw new DuplicateEmailException("Un compte existe déjà avec l'email : " + request.email());
        }

        Advertiser advertiser = new Advertiser(
                request.email(),
                request.nomEntreprise(),
                request.secteur(),
                request.siteWeb());
        Advertiser saved = advertiserRepository.save(advertiser);
        log.info("Advertiser created: id={}", saved.getId());
        return saved;
    }

    @Override
    public List<Advertiser> findAll() {
        return advertiserRepository.findAll();
    }

    @Override
    public Advertiser findById(Long id) {
        return advertiserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advertiser introuvable avec l'id : " + id));
    }
}
