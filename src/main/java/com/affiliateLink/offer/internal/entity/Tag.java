package com.affiliateLink.offer.internal.entity;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity 
@Table (name = "offer_tags")
public class Tag {

  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String name;

    //linkin menn offer b offer.replaceTags(...)
    @ManyToMany (mappedBy = "tags")
    private Set<Offer> offers = new HashSet<>();

    public Tag(){}
    public Tag(String name) {
        this.name = name;
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Set<Offer> getOffers() {
        return offers;
    }
    

    // وهكا "Java " و"JAVA" و"java" كلهم كيعطيو نفس الـ tag (“jamais dupliqués”)
    public static String normalize(String rawName) {
        return rawName.trim().toLowerCase(Locale.ROOT);// Locale.ROOT pour evité problem la langue de computer
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag other)) return false;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}