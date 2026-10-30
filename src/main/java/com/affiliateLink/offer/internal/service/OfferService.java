package com.affiliateLink.offer.internal.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.affiliateLink.offer.internal.dto.OfferCreateRequest;
import com.affiliateLink.offer.internal.dto.OfferResponse;
import com.affiliateLink.offer.internal.dto.OfferUpdateRequest;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.Tag;
import com.affiliateLink.offer.internal.mapper.OfferMapper;
import com.affiliateLink.offer.internal.repository.OfferRepository;
import com.affiliateLink.offer.internal.repository.TagRepository;
import com.affiliateLink.profile.ProfileApi; // الجذر ديال profile: مسموح

@Service
@Transactional(readOnly = true)
public class OfferService {

    private static final Logger log = LoggerFactory.getLogger(OfferService.class);

    private final OfferRepository offerRepository;
    private final TagRepository tagRepository;
    private final OfferMapper offerMapper;
    private final ProfileApi profileApi;

    public OfferService(OfferRepository offerRepository, TagRepository tagRepository,
            OfferMapper offerMapper, ProfileApi profileApi) {
        this.offerRepository = offerRepository;
        this.tagRepository = tagRepository;
        this.offerMapper = offerMapper;
        this.profileApi = profileApi;
    }

    @Transactional
    public OfferResponse create(OfferCreateRequest request) {
        Long advertiserId = request.advertiserId();

        // 1) واش الـ advertiser كاين؟ -> 404
        profileApi.getAdvertiser(advertiserId);

        // 2) واش نشيط؟ -> 409
        if (!profileApi.isActive(advertiserId)) {
            log.warn("Offer creation refused: advertiser {} is not active", advertiserId);
            throw new /* exception 409 ديالك */ RuntimeException("Advertiser is not active");
        }

        // 3) التحويل + الـ tags (كيحتاجو database، ماشي MapStruct)
        Offer offer = offerMapper.toEntity(request);
        offer.replaceTags(resolveTags(request.tags()));

        Offer saved = offerRepository.save(offer);
        log.info("Offer created: id={}, advertiserId={}", saved.getId(), advertiserId);

        return offerMapper.toResponse(saved); // داخل الـ transaction: ما كاينش LazyInitializationException
    }

    // كيرجع الـ tags الموجودين، وكيخلق غير اللي ما كايناش (jamais dupliqués)
    private Set<Tag> resolveTags(Set<String> rawNames) {
        Set<String> names = rawNames.stream()
                .map(Tag::normalize)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());

        if (names.isEmpty()) {
            return Set.of();
        }

        Map<String, Tag> existing = tagRepository.findByNameIn(names).stream()
                .collect(Collectors.toMap(Tag::getName, Function.identity()));

        Set<Tag> result = new HashSet<>(existing.values());
        for (String name : names) {
            if (!existing.containsKey(name)) {
                result.add(tagRepository.save(new Tag(name)));
            }
        }
        return result;
    }

    @Transactional
    public OfferResponse update(Long offerId, OfferUpdateRequest request) {
        // 1) العرض خاصو يكون كاين -> 404
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new /* exception 404 ديالك */ RuntimeException("Offer not found: " + offerId));

        // 2) الـ advertiser ديال هاد العرض (من العرض، ماشي من الـ request)
        Long advertiserId = offer.getAdvertiserId();
        if (!profileApi.isActive(advertiserId)) {
            log.warn("Offer update refused: advertiser {} is not active (offerId={})", advertiserId, offerId);
            throw new /* exception 409 ديالك */ RuntimeException("Advertiser is not active");
        }

        // 3) التعديل كيتدار فـ الـ entity
        offer.update(request.title(), request.description(), request.landingUrl(), request.niche());
        offer.replaceTags(resolveTags(request.tags()));

        // ما خاصناش offerRepository.save(): الـ entity managed، والـ dirty checking
        // كيكتب التعديلات عند الـ commit
        log.info("Offer updated: id={}, advertiserId={}", offerId, advertiserId);

        return offerMapper.toResponse(offer);
    }
}
