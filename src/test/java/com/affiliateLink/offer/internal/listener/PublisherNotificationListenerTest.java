package com.affiliateLink.offer.internal.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.affiliateLink.offer.internal.service.NotificationSender;
import com.affiliateLink.profile.AdvertiserInfo;
import com.affiliateLink.profile.ProfileApi;
import com.affiliateLink.profile.ProfileSuspended;

@ExtendWith(MockitoExtension.class)
class PublisherNotificationListenerTest {

    @Mock ProfileApi profileApi;
    @Mock NotificationSender notificationSender;
    @InjectMocks PublisherNotificationListener listener;

    @Test
    void notifiesActivePublishersWhenAnAdvertiserIsSuspended() {
        AdvertiserInfo advertiser = new AdvertiserInfo(7L, "a@x.com", "Acme", "Tech", "https://acme.com", false);
        when(profileApi.getAdvertiser(7L)).thenReturn(Optional.of(advertiser));
        when(profileApi.getActivePublisherEmails()).thenReturn(List.of("p1@x.com", "p2@x.com"));

        listener.on(new ProfileSuspended(7L));

        verify(notificationSender).send(eq(List.of("p1@x.com", "p2@x.com")), any(), any());
    }

    @Test
    void doesNothingWhenTheSuspendedProfileIsNotAnAdvertiser() {
        when(profileApi.getAdvertiser(9L)).thenReturn(Optional.empty());

        listener.on(new ProfileSuspended(9L));

        verify(notificationSender, never()).send(any(), any(), any());
    }

    @Test
    void doesNothingWhenThereIsNoActivePublisher() {
        AdvertiserInfo advertiser = new AdvertiserInfo(7L, "a@x.com", "Acme", "Tech", "https://acme.com", false);
        when(profileApi.getAdvertiser(7L)).thenReturn(Optional.of(advertiser));
        when(profileApi.getActivePublisherEmails()).thenReturn(List.of());

        listener.on(new ProfileSuspended(7L));

        verify(notificationSender, never()).send(any(), any(), any());
    }
}