package com.affiliateLink.profile.internal.dto;

import org.jspecify.annotations.NonNull;

import com.affiliateLink.profile.internal.entity.ProfileStatus;

public record StatusUpdateRequest (@NonNull ProfileStatus status){
    
}
