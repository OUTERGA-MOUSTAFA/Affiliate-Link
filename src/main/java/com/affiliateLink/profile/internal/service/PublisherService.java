package com.affiliateLink.profile.internal.service;

import java.util.List;

import com.affiliateLink.profile.internal.dto.PublisherCreateRequest;
import com.affiliateLink.profile.internal.entity.Publisher;

public interface PublisherService {
    Publisher create(PublisherCreateRequest request);

    List<Publisher> findAll();

    Publisher findById(Long id);
}
