package com.affiliateLink.offer.internal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.affiliateLink.offer.internal.dto.CampaignCreateRequest;
import com.affiliateLink.offer.internal.dto.CampaignResponse;
import com.affiliateLink.offer.internal.dto.CampaignStatusUpdateRequest;
import com.affiliateLink.offer.internal.entity.CampaignStatus;
import com.affiliateLink.offer.internal.service.impliments.CampaignServiceImpl;

import jakarta.validation.Valid;

@RestController("/api")
public class CampaignController {

    private final CampaignServiceImpl campaignService;

    public CampaignController(CampaignServiceImpl campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping("/offres/{offerId}/campaigns")
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse create(@PathVariable Long offerId,
            @Valid @RequestBody CampaignCreateRequest request) {
        return campaignService.create(offerId, request);
    }

    @GetMapping("/campaigns")
    public List<CampaignResponse> findAll(@RequestParam(required = false) CampaignStatus status) {
        return campaignService.findAll(status);
    }

    @PatchMapping("/campaigns/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(@PathVariable Long id,
            @Valid @RequestBody CampaignStatusUpdateRequest request) {
        campaignService.changeStatus(id, request.status());
    }
}