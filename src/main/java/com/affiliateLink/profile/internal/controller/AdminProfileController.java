package com.affiliateLink.profile.internal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.affiliateLink.profile.internal.dto.StatusUpdateRequest;
import com.affiliateLink.profile.internal.service.impliments.AdminProfileServiceImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminProfileController {

    private final AdminProfileServiceImpl adminProfileService;

    public AdminProfileController(AdminProfileServiceImpl adminProfileService) {
        this.adminProfileService = adminProfileService;
    }

    @PatchMapping("/profiles/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        adminProfileService.updateStatus(id, request.status());
        return ResponseEntity.noContent().build();
    }
}