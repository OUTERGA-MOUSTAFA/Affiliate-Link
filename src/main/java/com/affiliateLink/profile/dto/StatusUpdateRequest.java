package com.affiliateLink.profile.dto;

import org.jspecify.annotations.NonNull;

import com.affiliateLink.profile.entity.ProfileStatus;

public record StatusUpdateRequest (@NonNull ProfileStatus status){
    
}
