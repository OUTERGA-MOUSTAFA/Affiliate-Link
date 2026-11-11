package com.affiliateLink.profile.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.affiliateLink.common.DuplicateEmailException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.internal.dto.AdvertiserCreateRequest;
import com.affiliateLink.profile.internal.entity.Advertiser;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.repository.AdvertiserRepository;
import com.affiliateLink.profile.internal.repository.ProfileRepository;
import com.affiliateLink.profile.internal.service.impliments.AdvertiserServiceImpl;

@ExtendWith(MockitoExtension.class)            // كيشعل Mockito
class AdvertiserServiceImplTest {

    @Mock AdvertiserRepository advertiserRepository;
    @Mock ProfileRepository profileRepository;

    @InjectMocks AdvertiserServiceImpl advertiserService; // ③ البطل: كيتصاوب ب new، والممثلين كيدخلو ف constructor

    private final AdvertiserCreateRequest request =
            new AdvertiserCreateRequest("acme@mail.com", "Acme", "Tech", "https://acme.com");

    @Test
    void createdAdvertiserIsPending() {
        // Given
        when(profileRepository.existsByEmail("acme@mail.com")).thenReturn(false);
        when(advertiserRepository.save(any(Advertiser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // ④ رجع نفس الأوبجي اللي تصيفط

        // When
        Advertiser created = advertiserService.creaAdvertiser(request);

        // Then
        assertThat(created.getStatus()).isEqualTo(ProfileStatus.PENDING);
        assertThat(created.getEmail()).isEqualTo("acme@mail.com");
    }

    @Test
    void registrationWithUsedEmailIsRefusedAndNothingIsSaved() {
        // Given
        when(profileRepository.existsByEmail("acme@mail.com")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> advertiserService.creaAdvertiser(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("acme@mail.com");

        verify(advertiserRepository, never()).save(any());    //حتى بروفايل ما تسجل
    }

    @Test
    void findingUnknownAdvertiserThrowsNotFound() {
        // Given
        when(advertiserRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> advertiserService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}