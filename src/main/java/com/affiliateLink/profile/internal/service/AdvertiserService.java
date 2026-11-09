package com.affiliateLink.profile.internal.service;

import java.util.List;

import com.affiliateLink.profile.internal.dto.AdvertiserCreateRequest;
import com.affiliateLink.profile.internal.entity.Advertiser;

public interface AdvertiserService {
    Advertiser creaAdvertiser(AdvertiserCreateRequest request);

    List<Advertiser> findAll();

    Advertiser findById(Long id);
}
