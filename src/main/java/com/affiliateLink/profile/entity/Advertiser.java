package com.affiliateLink.profile.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_advertisers")
@PrimaryKeyJoinColumn(name = "id")
public class Advertiser extends Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false, length = 60)
    private String nomEntreprise;

    @Column(name = "sector", nullable = false, length = 40)
    private String secteur;

    @Column(name = "website", length = 100)
    private String siteWeb;

    public Advertiser(Long id, String email, ProfileStatus status, LocalDateTime createdAt, Long id2,
            String nomEntreprise, String secteur, String siteWeb) {
        super(id, email, status, createdAt);
        this.id = id2;
        this.nomEntreprise = nomEntreprise;
        this.secteur = secteur;
        this.siteWeb = siteWeb;
    }

    public Advertiser(Long id, String nomEntreprise, String secteur, String siteWeb) {
        this.id = id;
        this.nomEntreprise = nomEntreprise;
        this.secteur = secteur;
        this.siteWeb = siteWeb;
    }

    public Advertiser() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

}
