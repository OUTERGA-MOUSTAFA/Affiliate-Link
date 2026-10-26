package com.affiliateLink.internal.profile.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.affiliateLink.internal.profile.dto.PublisherCreateRequest;
import com.affiliateLink.internal.profile.dto.PublisherResponse;
import com.affiliateLink.internal.profile.service.PublisherService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

     @PostMapping("/publisher")
    @ResponseStatus(HttpStatus.CREATED)
    public PublisherResponse create(@Valid @RequestBody PublisherCreateRequest request) {
        return PublisherResponse.from(publisherService.create(request));
    }

    @GetMapping("publishers")
    public List<PublisherResponse> findAll() {
        return publisherService.findAll().stream()
                .map(PublisherResponse::from)
                .toList();
    }

    @GetMapping("/publisher/{id}")
    public PublisherResponse findById(@PathVariable Long id) {
        return PublisherResponse.from(publisherService.findById(id));
    }
   
}
