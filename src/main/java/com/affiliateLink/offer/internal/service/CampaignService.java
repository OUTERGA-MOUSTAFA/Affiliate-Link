package com.affiliateLink.offer.internal.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.affiliateLink.common.ConflictException;
import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.internal.dto.CampaignCreateRequest;
import com.affiliateLink.offer.internal.dto.CampaignResponse;
import com.affiliateLink.offer.internal.entity.Campaign;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.mapper.CampaignMapper;
import com.affiliateLink.offer.internal.repository.CampaignRepository;
import com.affiliateLink.offer.internal.repository.OfferRepository;
import com.affiliateLink.profile.ProfileApi;

@Service
@Transactional(readOnly = true) // les Queries li ghadare fhad class ghir dyale select safe.
// why: Performance Optimization
public class CampaignService {
    private static final Logger log = LoggerFactory.getLogger(CampaignService.class);// had ghir bash nhet logs f
                                                                                     // logs/affiliatelink.log

    private final CampaignRepository campaignRepository;
    private final OfferRepository offerRepository;
    private final CampaignMapper campaignMapper;
    private final ProfileApi profileApi;

    public CampaignService(CampaignRepository campaignRepository, OfferRepository offerRepository,
            CampaignMapper campaignMapper, ProfileApi profileApi) {

        this.campaignRepository = campaignRepository;
        this.offerRepository = offerRepository;
        this.campaignMapper = campaignMapper;
        this.profileApi = profileApi;
    }

    @Transactional
    public void changeStatus(Long campingnId, CampaignStatus targeStatus) {

        Campaign campaign = campaignRepository.findById(campingnId).orElseThrow(
                () -> new ResourceNotFoundException("Campaign not found: " + campingnId));

        switch (targeStatus) {
            case ACTIVE -> activate(campaign);
            case PAUSED -> pause();
            case ENDED -> end();
            default -> throw new InvalidStatusTransitionException("A campaign cannot go back to " + targeStatus);
        }
        log.info("Campaign {} is now {}", campingnId, campaign.getStatus());
    }

    // هذه الدالة تحتوي على شروط (Validations)، فإذا كان التاريخ قد فات أو الانتقال
    // ممنوع، فإنها ترمي استثناءً (Exception).
    private void activate(Campaign campaign) {
        campaignRepository.findByOfferIdAndStatus(campaign.getOffer().getId(), CampaignStatus.ACTIVE)
                .filter(current -> !current.getId().equals(campaign.getId()))
                .ifPresent(current -> {
                    current.pause();
                    campaignRepository.saveAndFlush(current); // إلى قاعدة البيانات دون الانتظار حتى تنتهي الدالة تعني
                                                              // حفظ التعديلات وإرسالها فوراً
                    // why not save: bc hebernit يحتفظ بالتعديل في الذاكرة (تسمى Persistence
                    // Context) till method end and transaction be seccus then send it as Commit
                    log.info("Campaign {} paused: replaced by {}", current.getId(), campaign.getId());
                });

        campaign.activate(); // كترمي exception إلا الانتقال ممنوع أو التاريخ فات
    }

    @Transactional
    public CampaignResponse create(Long offerId, CampaignCreateRequest request) {
        // 1) العرض خاصو يكون كاين -> 404
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + offerId));

        // 2) الـ advertiser ديال هاد العرض خاصو يكون نشيط -> 409
        Long advertiserId = offer.getAdvertiserId();
        if (!profileApi.isActive(advertiserId)) {
            log.warn("Campaign creation refused: advertiser {} is not active (offerId={})", advertiserId, offerId);
            throw new ConflictException("Advertiser is not active");
        }

        // 3) التحويل: الحملة كتتخلق DRAFT، وكنربطوها بالعرض
        Campaign campaign = campaignMapper.toEntity(request);
        offer.addCampaign(campaign);

        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaign created: id={}, offerId={}, model={}", saved.getId(), offerId, saved.getPayoutModel());

        return campaignMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> findByStatus(CampaignStatus status) {
        return campaignRepository.findByStatus(status).stream()
                .map(campaignMapper::toResponse)
                .toList();
    }
}
