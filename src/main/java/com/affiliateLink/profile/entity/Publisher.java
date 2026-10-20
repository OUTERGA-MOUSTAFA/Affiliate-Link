package com.affiliateLink.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_publishers")
@PrimaryKeyJoinColumn(name = "id")
public class Publisher extends Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 40)
    private String nom;

    @Column(name = "last_name", nullable = false, length = 40)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Niche niche;

    @Column(length = 500, nullable = false)
    private String bio;

    @Column(nullable = false, length = 100)
    private String paye;

    public Publisher() {
    }

    public Publisher(Long id, String nom,
            String prenom, Niche niche,
            String bio, String paye) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.niche = niche;
        this.bio = bio;
        this.paye = paye;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPaye(String paye) {
        this.paye = paye;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Niche getNiche() {
        return niche;
    }

    public void setNiche(Niche niche) {
        this.niche = niche;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPaye() {
        return paye;
    }

}
