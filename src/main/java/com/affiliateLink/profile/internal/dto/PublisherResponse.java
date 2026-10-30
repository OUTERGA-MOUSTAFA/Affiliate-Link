package com.affiliateLink.profile.internal.dto;

import java.time.LocalDateTime;

import com.affiliateLink.common.Niche;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.entity.Publisher;

public record PublisherResponse(Long id,
        String email,
        ProfileStatus status,
        LocalDateTime createdAt,
        String firstName,
        String lastName,
        Niche niche,
        String bio,
        String country) {

    public static PublisherResponse from(Publisher p) {
        return new PublisherResponse(
                p.getId(), p.getEmail(), p.getStatus(), p.getCreatedAt(),
                p.getFirstName(), p.getLastName(), p.getNiche(), p.getBio(), p.getCountry());
    }
}
