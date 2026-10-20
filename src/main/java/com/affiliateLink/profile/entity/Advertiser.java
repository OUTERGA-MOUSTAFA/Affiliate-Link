package com.affiliateLink.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table (name = "profile_advertisers")
@PrimaryKeyJoinColumn (name = "id")
public class Advertiser extends Profile{
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "company_name", nullable = false, length = 60)
    private String nomEntreprise;

    @Column (name = "sector", nullable = false, length = 40)
    private String secteur;

    @Column (name = "website", length = 100)
    private String siteWeb;

    public Advertiser(){}
}
