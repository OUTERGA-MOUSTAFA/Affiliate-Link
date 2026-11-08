package com.affiliateLink.profile;

import java.util.Optional;
public interface ProfileApi {

    Optional<AdvertiserInfo> getAdvertiser(Long id);
    // Advertise n'existe pas -> 404 Not Found
    // ecvertise exist mais pas actif (PENDING or SUSPENDED) -> 409 Conflict

    default boolean isActive(Long id) {
        return getAdvertiser(id).map(AdvertiserInfo::active).orElse(false);
    }
}
