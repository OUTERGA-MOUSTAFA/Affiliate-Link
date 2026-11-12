package com.affiliateLink.profile.internal.dto;

// import org.jspecify.annotations.NonNull;
import jakarta.validation.constraints.NotNull;      // ← jakarta، ماشي jspecify

import com.affiliateLink.profile.internal.entity.ProfileStatus;

public record StatusUpdateRequest (@NotNull ProfileStatus status){
    
}
