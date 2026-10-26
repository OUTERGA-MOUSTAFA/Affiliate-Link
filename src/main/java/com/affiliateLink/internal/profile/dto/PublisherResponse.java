package com.affiliateLink.internal.profile.dto;

import java.time.LocalDateTime;

import com.affiliateLink.internal.profile.entity.Niche;
import com.affiliateLink.internal.profile.entity.ProfileStatus;
import com.affiliateLink.internal.profile.entity.Publisher;

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
