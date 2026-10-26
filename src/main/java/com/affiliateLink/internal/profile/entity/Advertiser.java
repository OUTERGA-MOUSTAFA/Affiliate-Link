package com.affiliateLink.internal.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_advertisers")
@PrimaryKeyJoinColumn(name = "id")
public class Advertiser extends Profile {

    @Column(name = "company_name", nullable = false, length = 60)
    private String nomEntreprise;

    @Column(name = "sector", nullable = false, length = 40)
    private String secteur;

    @Column(name = "website", length = 100)
    private String siteWeb;

    public Advertiser() {
    }

    public Advertiser(String email, String nomEntreprise, String secteur, String siteWeb) {
        super(email);
        this.nomEntreprise = nomEntreprise;
        this.secteur = secteur;
        this.siteWeb = siteWeb;
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