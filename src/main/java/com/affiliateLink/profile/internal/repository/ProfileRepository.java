package com.affiliateLink.profile.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.affiliateLink.profile.internal.entity.Profile;

public interface ProfileRepository extends JpaRepository <Profile, Long> {
     boolean existsByEmail(String email);
}
