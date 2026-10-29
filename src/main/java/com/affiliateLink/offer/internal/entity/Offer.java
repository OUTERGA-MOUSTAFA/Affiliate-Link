package com.affiliateLink.offer.internal.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "offer_offers")
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "advertiser_id", nullable = false, updatable = false)
    private Long advertiserId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "landing_url", nullable = false, length = 500)
    private String landingUrl;

    @Column(nullable = false, length = 20)
    private String niche;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Offer() {
    }

    public Offer(Long advertiserId, String title, String description, String landingUrl, String niche,
            Instant createdAt) {
        this.advertiserId = advertiserId;
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
        this.niche = niche;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getAdvertiserId() {
        return advertiserId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLandingUrl() {
        return landingUrl;
    }

    public String getNiche() {
        return niche;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // private List<Campaign> campaigns = new ArrayList<>();

    // private Set<Tag> tags = new HashSet<>();

}
