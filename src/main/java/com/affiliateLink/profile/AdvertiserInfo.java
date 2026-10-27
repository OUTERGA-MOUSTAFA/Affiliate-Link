package com.affiliateLink.profile;

public record AdvertiserInfo(
        Long id,
        String email,
        String companyName,
        boolean active) {
}