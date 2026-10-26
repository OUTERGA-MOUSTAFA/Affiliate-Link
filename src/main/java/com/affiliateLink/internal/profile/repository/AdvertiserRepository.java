package com.affiliateLink.internal.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.affiliateLink.internal.profile.entity.Advertiser;

public interface AdvertiserRepository extends JpaRepository<Advertiser, Long>{
    
}
