package com.affiliateLink.profile.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.affiliateLink.profile.internal.entity.Advertiser;

public interface AdvertiserRepository extends JpaRepository<Advertiser, Long>{
    
}
