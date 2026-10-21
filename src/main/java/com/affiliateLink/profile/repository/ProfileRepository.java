package com.affiliateLink.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.affiliateLink.profile.entity.Profile;

public interface ProfileRepository extends JpaRepository <Profile, Long> {
     boolean existsByEmail(String email);
}
