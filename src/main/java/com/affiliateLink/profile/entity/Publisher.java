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
public class Publisher {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 40 )
    private String nom;

    @Column(name = "last_name", nullable = false, length = 40 )
    private String prenom;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false, length = 20)
    private Niche niche;

    @Column (length = 500, nullable = false)
    private String bio;

    @Column(nullable = false, length = 100)
    private String paye;

    public Publisher(){}
}
