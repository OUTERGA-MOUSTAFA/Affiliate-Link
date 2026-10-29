package com.affiliateLink.offer.internal.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
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

    // la relation avec table offer
    @OneToMany (mappedBy = "Offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Campaign> campaings = new ArrayList<>();

    // la relation avec table tags
    @ManyToMany 
    @JoinTable (name = "offer_offer_tags", 
                joinColumns = @JoinColumn(name ="offer_id"), 
                inverseJoinColumns = @JoinColumn (name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    public Offer() {
    }

    public Offer(Long advertiserId, String title, String description, String landingUrl, String niche,
            Instant createdAt) {
        this.advertiserId = advertiserId;
        this.createdAt = Instant.now();
        update(title, description, landingUrl, niche);
    }

    public void replaceTags(Set<Tag> newTag){
        this.tags.clear();
        this.tags.addAll(newTag);
    }
    public void update(String title, String description, String landingUrl, String niche) {
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
        this.niche = niche;
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

     private List<Campaign> getCoCampaigns(){
        return Collections.unmodifiableList(campaings);
     }

    private Set<Tag> tags(){
        return Collections.unmodifiableSet(tags);
    }

}
