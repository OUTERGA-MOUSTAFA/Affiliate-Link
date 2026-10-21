package com.affiliateLink.profile.entity;

import java.time.LocalDateTime;

import com.affiliateLink.common.InvalidStatusTransitionException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_profiles")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProfileStatus status;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public Profile(String email) {
        this.id = id;
        this.email = email;
        this.status = status.PENDING; //tout nouveau compte derict status pending
        this.createdAt = createdAt.now();
    }

    public Profile() {// hada dyal jpa
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public ProfileStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private void transitionTo(ProfileStatus target, ProfileStatus requiredCurrent) {
        if (this.status != requiredCurrent) {
            throw new InvalidStatusTransitionException(
                "Impossible de passer de " + this.status + " à " + target +
                " (statut requis : " + requiredCurrent + ")"
            );
        }
        this.status = target;
    }

    public void activate(){
        
        transitionTo(ProfileStatus.ACTIVE, ProfileStatus.PENDING);
    }

    public void suspend() {
        transitionTo(ProfileStatus.SUSPENDED, ProfileStatus.ACTIVE);
    }

    public void reactivate() {
        transitionTo(ProfileStatus.ACTIVE, ProfileStatus.SUSPENDED);
    }



}
