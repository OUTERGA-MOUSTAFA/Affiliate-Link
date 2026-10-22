package com.affiliateLink.profile.dto;

import java.time.LocalDateTime;

import com.affiliateLink.profile.entity.Advertiser;
import com.affiliateLink.profile.entity.ProfileStatus;

public record AdvertiserResponse(
       Long id,
        String email,
        String nomEntreprise,
        String secteur,
        String siteWeb,
        ProfileStatus status,
        LocalDateTime createdAt) {

    public static AdvertiserResponse from(Advertiser adv) {
        return new AdvertiserResponse(
                adv.getId(),
                adv.getEmail(),
                adv.getNomEntreprise(),
                adv.getSecteur(),
                adv.getSiteWeb(),
                adv.getStatus(),
                adv.getCreatedAt());
    }
}
