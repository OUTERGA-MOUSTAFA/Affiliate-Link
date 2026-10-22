package com.affiliateLink.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.affiliateLink.profile.entity.Advertiser;

public interface AdvertiserRepository extends JpaRepository<Advertiser, Long>{
    
}
