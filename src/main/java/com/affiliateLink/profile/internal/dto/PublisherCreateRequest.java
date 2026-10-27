package com.affiliateLink.profile.internal.dto;

import com.affiliateLink.profile.internal.entity.Niche;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PublisherCreateRequest( @NotBlank @Email String email,
     @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Niche niche,
        String bio,
        @NotBlank String country) {
   
}
