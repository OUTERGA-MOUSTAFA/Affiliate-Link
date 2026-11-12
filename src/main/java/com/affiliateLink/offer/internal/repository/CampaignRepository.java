package com.affiliateLink.offer.internal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.CampaignStatus;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByOfferIdAndStatus(Long offerId, CampaignStatus status);

    List<Campaign> findByStatus(CampaignStatus status);

    // listener (offer.advertiserId)
    List<Campaign> findByOfferAdvertiserIdAndStatus(Long advertiserId, CampaignStatus status);

    // Écrire une requête qui renvoie les campagnes d’un advertiser dans un statut
    // donné, triées par date de début.

    @Query (""" 
        Select c. Compaign  c where c.offer.advertiserId=:advertiserId and c.status=: status Order By c.startDate
    """)

    List<Campaign> findByAdvertiserIdAndStatusOrderByStartDate(@Param("advertiserId")  Long advertiserId,@Param ("status") CampaignStatus status);
}
