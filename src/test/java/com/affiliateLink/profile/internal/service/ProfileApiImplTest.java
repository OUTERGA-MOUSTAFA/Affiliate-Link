package com.affiliateLink.profile.internal.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.affiliateLink.profile.internal.entity.Advertiser;
import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.repository.AdvertiserRepository;

@ExtendWith(MockitoExtension.class)
class ProfileApiImplTest {

    @Mock
    AdvertiserRepository advertiserRepository;
    @InjectMocks
    ProfileApiImpl profileApi;

    @Test
    void isActiveReturnsFalseWhenAdvertiserDoesNotExist() {
        when(advertiserRepository.findById(99L)).thenReturn(Optional.empty());

        assertFalse(profileApi.isActive(99L));
    }

    @Test
    void isActiveReturnsTrueForActiveAdvertiser() {
        Advertiser advertiser = mock(Advertiser.class);
        when(advertiser.getStatus()).thenReturn(ProfileStatus.ACTIVE);
        when(advertiserRepository.findById(7L)).thenReturn(Optional.of(advertiser));

        assertTrue(profileApi.isActive(7L));
    }

    @Test
    void getAdvertiserReturnsEmptyWhenNotFound() {
        when(advertiserRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(profileApi.getAdvertiser(99L).isEmpty());
    }
}