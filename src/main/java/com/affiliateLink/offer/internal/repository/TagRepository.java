package com.affiliateLink.offer.internal.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.affiliateLink.offer.internal.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long>{
     List<Tag> findByNameIn(Collection<String> names);
}
