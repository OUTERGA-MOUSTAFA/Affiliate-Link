package com.affiliateLink.offer.internal.repository;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.affiliateLink.common.Niche;   // بدلو على حسب فين كاين enum Niche عندك
import com.affiliateLink.offer.internal.entity.Offer;
import com.affiliateLink.offer.internal.entity.Tag;

import jakarta.persistence.criteria.Join;

public final class OfferSpecifications {

    private OfferSpecifications() {
    }

    // كل باراميتر null (ولا فارغ) كيتتجاهل. 
    public static Specification<Offer> search(Niche niche, Long advertiserId, String tag, String keyword) {
        Specification<Offer> spec = (root, query, cb) -> cb.conjunction();   // أصل خاوي = بلا فلتر

        if (niche != null) {
            spec = spec.and(hasNiche(niche));
        }
        if (advertiserId != null) {
            spec = spec.and(hasAdvertiser(advertiserId));
        }
        if (tag != null && !tag.isBlank()) {
            spec = spec.and(hasTag(tag));
        }
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(titleContains(keyword));
        }
        return spec;
    }

    private static Specification<Offer> hasNiche(Niche niche) {
        return (root, query, cb) -> cb.equal(root.get("niche"), niche);
    }

    private static Specification<Offer> hasAdvertiser(Long advertiserId) {
        return (root, query, cb) -> cb.equal(root.get("advertiserId"), advertiserId);
    }

    private static Specification<Offer> hasTag(String tag) {
        String normalized = tag.trim().toLowerCase(Locale.ROOT);   // الـ tags محفوظين بحروف صغيرة
        return (root, query, cb) -> {
            Join<Offer, Tag> tags = root.join("tags");
            return cb.equal(tags.get("name"), normalized);
        };
    }

    private static Specification<Offer> titleContains(String keyword) {
        String pattern = "%" + escapeLike(keyword.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.<String>get("title")), pattern, '\\');
    }

    // باش % و _ اللي كيكتبهم المستعمل يتعاملو كحروف عادية
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}