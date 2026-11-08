package com.affiliateLink.profile.internal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.affiliateLink.profile.internal.entity.ProfileStatus;
import com.affiliateLink.profile.internal.entity.Publisher;

public interface PublisherRepository extends JpaRepository <Publisher, Long>{
        List<Publisher> findActivePublisherByEmails(ProfileStatus status);

}
