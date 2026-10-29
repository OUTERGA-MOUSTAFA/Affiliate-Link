package com.affiliateLink.offer.internal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.affiliateLink.offer.internal.dto.StatusRequest;
import com.affiliateLink.offer.internal.service.CampaignService;

import jakarta.validation.Valid;

@RestController
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PatchMapping("/api/campaigns/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        campaignService.changeStatus(id, request.status());
    }

}
