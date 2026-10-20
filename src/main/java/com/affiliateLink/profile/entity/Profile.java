package com.affiliateLink.profile.entity;

import java.time.LocalDateTime;

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
@Table (name = "profile_profiles")
@Inheritance(strategy = InheritanceType.JOINED)
public class Profile {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, unique = true)
    private String email;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false, length = 20)
    private ProfileStatus status;

    @Column (name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public Profile() {  }

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


}
