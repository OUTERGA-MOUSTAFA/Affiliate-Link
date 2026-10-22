package com.affiliateLink.profile.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.affiliateLink.profile.dto.PublisherCreateRequest;
import com.affiliateLink.profile.dto.PublisherResponse;
import com.affiliateLink.profile.service.PublisherService;

import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;

@RestController
@RequestMapping("/api")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @GetMapping ("/publisher/{id}")
    // @ResponseStatus (HttpStatus.OK)
    public PublisherResponse getPublisher(@PathVariable Long id) {
        return PublisherResponse.from(publisherService.findById(id));
    }

    @GetMapping ("/publishers")
    // @ResponseStatus (HttpStatus.OK)
    public List<PublisherResponse> getAllPublisher() {
        return publisherService.findAll().stream().map(PublisherResponse::from).toList();
    }

    @PostMapping("/publisher")
    @ResponseStatus(HttpStatus.CREATED)
    public PublisherResponse createPublisher(@Valid @RequestBody PublisherCreateRequest request) {
        return PublisherResponse.from(publisherService.create(request));
    }

   
}
