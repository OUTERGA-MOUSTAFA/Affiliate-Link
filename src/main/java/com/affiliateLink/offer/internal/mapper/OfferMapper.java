package com.affiliateLink.offer.internal.mapper;

import com.affiliateLink.offer.internal.dto.OfferRequest;
import com.affiliateLink.offer.internal.dto.OfferResponse;
import com.affiliateLink.offer.internal.dto.OfferSummaryResponse;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.Tag;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CampaignMapper.class)
public interface OfferMapper {

    // tags و campaigns كيتعمرو فـ الـ service (كيحتاجو database)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "campaigns", ignore = true)
    Offer toEntity(OfferRequest request);

    OfferResponse toResponse(Offer offer);

    OfferSummaryResponse toSummary(Offer offer);

    // MapStruct كيستعملها لتحويل Set<Tag> إلى Set<String>
    default String toName(Tag tag) {
        return tag.getName();
    }
}