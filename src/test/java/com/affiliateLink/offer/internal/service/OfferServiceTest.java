package com.affiliateLink.offer.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.affiliateLink.common.Niche;
import com.affiliateLink.offer.internal.dto.OfferUpdateRequest;
import com.affiliateLink.offer.internal.entity.Offer;

public class OfferServiceTest {

    // @Test
    // void updateModifiesExistingOfferWithoutCreatingANewOne() {
    // Offer offer = new Offer(7L, "Old", "d", "https://old.example.com",
    // Niche.TECH);
    // OfferUpdateRequest request = new OfferUpdateRequest(
    // "New title", "d", "https://new.example.com", Niche.GAMING, Set.of());

    // when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
    // when(profileApi.isActive(7L)).thenReturn(true);

    // offerService.update(1L, request);

    // assertEquals("New title", offer.getTitle());
    // assertEquals(Niche.GAMING, offer.getNiche());
    // assertEquals(7L, offer.getAdvertiserId()); // ما تبدلش
    // verify(offerRepository, never()).save(any()); // ما كاينش INSERT
    // }

    // @Test
    // void updateOfUnknownOfferThrows() {
    // when(offerRepository.findById(99L)).thenReturn(Optional.empty());

    // assertThrows(RuntimeException.class, // بدل exception 404
    // () -> offerService.update(99L,
    // new OfferUpdateRequest("t", null, "https://x.com", Niche.TECH, Set.of())));
    // }

    // @Test
    // void updateRefusedWhenAdvertiserIsNotActive() {
    // Offer offer = new Offer(7L, "Old", "d", "https://old.example.com",
    // Niche.TECH);
    // when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
    // when(profileApi.isActive(7L)).thenReturn(false);

    // assertThrows(RuntimeException.class, // بدل exception 409
    // () -> offerService.update(1L,
    // new OfferUpdateRequest("New", null, "https://x.com", Niche.TECH, Set.of())));

    // assertEquals("Old", offer.getTitle()); // ما تبدل والو
    // }
}
