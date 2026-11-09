package com.affiliateLink.profile.internal.service.impliments;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.affiliateLink.common.DuplicateEmailException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.internal.dto.PublisherCreateRequest;
import com.affiliateLink.profile.internal.entity.Publisher;
import com.affiliateLink.profile.internal.repository.ProfileRepository;
import com.affiliateLink.profile.internal.repository.PublisherRepository;
import com.affiliateLink.profile.internal.service.PublisherService;

@Service
@Transactional(readOnly = true)
public class PublisherServiceImpl implements PublisherService{

    private static final Logger log = LoggerFactory.getLogger(PublisherServiceImpl.class);

    private final PublisherRepository publisherRepository;
    private final ProfileRepository profileRepository;

    public PublisherServiceImpl(PublisherRepository publisherRepository, ProfileRepository profileRepository) {
        this.publisherRepository = publisherRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public Publisher create(PublisherCreateRequest request) {

        // check unicité (publishers + advertisers)
        if (profileRepository.existsByEmail(request.email())) {
            log.warn("Publisher creation refused: email already used");
            throw new DuplicateEmailException("Un compte existe déjà avec l'email : " + request.email());
        }
        Publisher publisher = new Publisher(
                request.email(), request.firstName(), request.lastName(),
                request.niche(), request.bio(), request.country());
                // status = PENDING
        Publisher saved = publisherRepository.save(publisher);
        log.info("Publisher created: id={}", saved.getId());
        return saved;
    }

    @Override
    public List<Publisher> findAll() {
        return publisherRepository.findAll();
    }

    @Override 
    public Publisher findById(Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher introuvable avec l'id : " + id));
    }
}
