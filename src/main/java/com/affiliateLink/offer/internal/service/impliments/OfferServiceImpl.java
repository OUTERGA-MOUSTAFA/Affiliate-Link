package com.affiliateLink.offer.internal.service.impliments;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.affiliateLink.common.Niche;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.internal.dto.OfferCreateRequest;
import com.affiliateLink.offer.internal.dto.OfferResponse;
import com.affiliateLink.offer.internal.dto.OfferSummaryResponse;
import com.affiliateLink.offer.internal.dto.OfferUpdateRequest;
import com.affiliateLink.offer.internal.dto.PageResponse;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.Tag;
import com.affiliateLink.offer.internal.mapper.OfferMapper;
import com.affiliateLink.offer.internal.repository.OfferRepository;
import com.affiliateLink.offer.internal.repository.OfferSpecifications;
import com.affiliateLink.offer.internal.repository.TagRepository;
import com.affiliateLink.offer.internal.service.OfferService;
import com.affiliateLink.profile.ProfileApi; // الجذر ديال profile: مسموح

@Service
@Transactional(readOnly = true)
public class OfferServiceImpl implements OfferService{

    private static final Logger log = LoggerFactory.getLogger(OfferServiceImpl.class);

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "title", "niche", "createdAt", "advertiserId");
    private final OfferRepository offerRepository;
    private final TagRepository tagRepository;
    private final OfferMapper offerMapper;
    private final ProfileApi profileApi;

    public OfferServiceImpl(OfferRepository offerRepository, TagRepository tagRepository,
            OfferMapper offerMapper, ProfileApi profileApi) {
        this.offerRepository = offerRepository;
        this.tagRepository = tagRepository;
        this.offerMapper = offerMapper;
        this.profileApi = profileApi;
    }

    @Override 
    @Transactional
    public OfferResponse create(OfferCreateRequest request) {
        Long advertiserId = request.advertiserId();

        // wash kayn be3da
        profileApi.getAdvertiser(advertiserId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertiser not found: " + advertiserId));

        // 2) واش نشيط؟ ->est ce qu'il active
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

    @Override 
    @Transactional
    public OfferResponse update(Long offerId, OfferUpdateRequest request) {
        // 
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new /* exception 404 */ RuntimeException("Offer not found: " + offerId));

        // advertise of this offer pas de request
        Long advertiserId = offer.getAdvertiserId();
        if (!profileApi.isActive(advertiserId)) {
            log.warn("Offer update refused: advertiser {} is not active (offerId={})", advertiserId, offerId);
            throw new /* exception 409 */ RuntimeException("Advertiser is not active");
        }

        // modification f entity
        // ما خاصناش offerRepository.save(): الـ entity managed، والـ dirty checking
        // كيكتب التعديلات عند الـ commit
        offer.update(request.title(), request.description(), request.landingUrl(), request.niche());
        offer.replaceTags(resolveTags(request.tags()));

        log.info("Offer updated: id={}, advertiserId={}", offerId, advertiserId);

        return offerMapper.toResponse(offer);
    }

    @Override
    public OfferResponse getById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new /* exception 404 */ RuntimeException("Offer not found: " + id));

        return offerMapper.toResponse(offer); // OfferResponse فيه الحملات (campaigns)
    }

    @Override
    public PageResponse<OfferSummaryResponse> search(Niche niche, Long advertiserId, String tag,
            String keyword, Pageable pageable) {
        validateSort(pageable.getSort());

        // بلا sort؟ الأحدث أولاً
        Pageable effective = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Offer> page = offerRepository.findAll(
                OfferSpecifications.search(niche, advertiserId, tag, keyword), effective);

        log.debug("Offer search: niche={}, advertiserId={}, tag={}, keyword={} -> {} result(s)",
                niche, advertiserId, tag, keyword, page.getTotalElements());

        // ستعمل الدالة toSummary ديال الـ mapper". وهي نفسها كتب page.map(offer ->
        // offerMapper.toSummary(offer))
        return PageResponse.from(page.map(offerMapper::toSummary)); // داخل الـ transaction
        // {
        // "content": [ ...10 عروض... ],
        // "page": 0,
        // "size": 10,
        // "totalElements": 95,
        // "totalPages": 10
        // }
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                log.warn("Offer search refused: unsupported sort field '{}'", order.getProperty());
                throw new /* exception 400 */ IllegalArgumentException(
                        "Unsupported sort field: " + order.getProperty()
                                + ". Allowed: " + SORTABLE_FIELDS);
            }
        }
    }
}
