package com.affiliateLink.internal.profile.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdvertiserCreateRequest(
        @NotBlank @Email String email,
        @NotBlank @NotNull @Length(max = 60) String nomEntreprise,
        @NotBlank @NotNull @Length(max = 40) String secteur,
        @Length(max = 100) String siteWeb) {}
