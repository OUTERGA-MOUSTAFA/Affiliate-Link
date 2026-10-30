package com.affiliateLink.offer.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.affiliateLink.offer.internal.entity.Offer;

public interface OfferRepository extends JpaRepository<Offer, Long> , JpaSpecificationExecutor<Offer>{
    //  JpaSpecificationExecutor هو اللي كيعطيك findAll(Specification, Pageable). بلاه ما كاين بحث ديناميكي.
}
