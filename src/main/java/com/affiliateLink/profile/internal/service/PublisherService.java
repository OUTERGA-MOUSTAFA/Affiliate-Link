package com.affiliateLink.profile.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.affiliateLink.common.DuplicateEmailException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.internal.dto.PublisherCreateRequest;
import com.affiliateLink.profile.internal.entity.Publisher;
import com.affiliateLink.profile.internal.repository.ProfileRepository;
import com.affiliateLink.profile.internal.repository.PublisherRepository;

@Service
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final ProfileRepository profileRepository;

    public PublisherService(PublisherRepository publisherRepository, ProfileRepository profileRepository) {
        this.publisherRepository = publisherRepository;
        this.profileRepository = profileRepository;
    }

    public Publisher create(PublisherCreateRequest request) {

        //check unicité (publishers + advertisers)
        if (profileRepository.existsByEmail(request.email())) {
           throw new DuplicateEmailException("Un compte existe déjà avec l'email : " + request.email());
        }
        Publisher publisher = new Publisher(
                request.email(), request.firstName(), request.lastName(),
                request.niche(), request.bio(), request.country());
                //status = PENDING 
        return publisherRepository.save(publisher);
    }

    public List<Publisher> findAll() {
        return publisherRepository.findAll();
    }

    public Publisher findById(Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher introuvable avec l'id : " + id));
    }
}
