package com.lumina.backend.donation.repository;

import com.lumina.backend.donation.model.entity.UserDonation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDonationRepository extends JpaRepository<UserDonation, Long> {

    boolean existsByUserIdAndDonationIdAndRegistration(Long userId, Long donationId, String registration);

    Optional<UserDonation> findByUserIdAndDonationId(Long userId, Long donationId);
}
