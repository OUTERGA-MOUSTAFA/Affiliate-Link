package com.affiliateLink.internal.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.affiliateLink.internal.profile.entity.Profile;

public interface ProfileRepository extends JpaRepository <Profile, Long> {
     boolean existsByEmail(String email);
}
