package com.affiliateLink.profile.internal.service;

import com.affiliateLink.profile.internal.entity.ProfileStatus;

public interface AdminProfileService {
    void updateStatus(Long id, ProfileStatus targetStatus);
}
