package com.affiliateLink.profile.internal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.affiliateLink.profile.internal.dto.AdvertiserCreateRequest;
import com.affiliateLink.profile.internal.dto.AdvertiserResponse;
import com.affiliateLink.profile.internal.service.AdvertiserService;

import jakarta.validation.Valid;


@RestController  
@RequestMapping ("/api")
public class AdvertiserController {
    private final AdvertiserService advertiserService;
    public AdvertiserController(AdvertiserService advertiserService){
        this.advertiserService = advertiserService;
    }

    @GetMapping ("/advertisers")
    public List<AdvertiserResponse> getAllAdvertisers(){
        return advertiserService.findAll().stream().map(AdvertiserResponse::from).toList();
    }

    @GetMapping ("/advertiser/{id}")
    public AdvertiserResponse getAdvertiserById(@PathVariable Long id){
        return AdvertiserResponse.from(advertiserService.findById(id));
    }

    @PostMapping ("/advertiser")
    @ResponseStatus(HttpStatus.CREATED)
    public AdvertiserResponse creaAdvertiser(@Valid @RequestBody AdvertiserCreateRequest request){

        return AdvertiserResponse.from(advertiserService.creaAdvertiser(request));
    }
}
