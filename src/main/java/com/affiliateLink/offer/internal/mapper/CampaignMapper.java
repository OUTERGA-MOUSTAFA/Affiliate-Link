package com.affiliateLink.offer.internal.mapper;

import com.affiliateLink.offer.internal.dto.CampaignRequest;
import com.affiliateLink.offer.internal.dto.CampaignResponse;
import com.affiliateLink.offer.internal.entity.Campaign;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignMapper {

    // الـ offer كيتربط من بعد بـ offer.addCampaign(...) فـ الـ service
    Campaign toEntity(CampaignRequest request);

    @Mapping(target = "offerId", source = "offer.id")
    CampaignResponse toResponse(Campaign campaign);
}