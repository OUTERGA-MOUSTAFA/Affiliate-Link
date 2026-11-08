package com.affiliateLink.profile;

public record AdvertiserInfo(
        Long id,
        String email,
        String companyName,
        String sector,
        String website,
        boolean active) {
}