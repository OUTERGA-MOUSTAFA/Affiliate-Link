package com.affiliateLink.internal.profile.dto;

import org.jspecify.annotations.NonNull;

import com.affiliateLink.internal.profile.entity.ProfileStatus;

public record StatusUpdateRequest (@NonNull ProfileStatus status){
    
}
