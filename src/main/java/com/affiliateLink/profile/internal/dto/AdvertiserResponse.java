package com.affiliateLink.profile.internal.dto;

import java.time.LocalDateTime;

import com.affiliateLink.profile.internal.entity.Advertiser;
import com.affiliateLink.profile.internal.entity.ProfileStatus;

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
