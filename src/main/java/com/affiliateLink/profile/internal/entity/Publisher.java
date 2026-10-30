package com.affiliateLink.profile.internal.entity;

import org.springframework.context.annotation.Configuration;

import com.affiliateLink.common.Niche;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
@Configuration 
@Entity
@Table(name = "profile_publishers")
@PrimaryKeyJoinColumn(name = "id")
public class Publisher extends Profile {

    @Column(name = "first_name", nullable = false, length = 40)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 40)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Niche niche;

    @Column(length = 500)
    private String bio;

    @Column(nullable = false, length = 100)
    private String country;

    public Publisher() {
    }

    public Publisher(String email, String firstName, String lastName, Niche niche, String bio, String country) {
        super(email);
        this.firstName = firstName;
        this.lastName = lastName;
        this.niche = niche;
        this.bio = bio;
        this.country = country;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Niche getNiche() { return niche; }
    public void setNiche(Niche niche) { this.niche = niche; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}