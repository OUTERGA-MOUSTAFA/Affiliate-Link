package com.affiliateLink.offer.internal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.CampaignStatus;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByOfferIdAndStatus(Long offerId, CampaignStatus status);
    List<Campaign> findByStatus(CampaignStatus status);
    // listener (offer.advertiserId)
    List<Campaign> findByOfferAdvertiserIdAndStatus(Long advertiserId, CampaignStatus status);
}
