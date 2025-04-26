package com.lumina.backend.donation.repository;

import com.lumina.backend.donation.model.entity.UserDonation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDonationRepository extends JpaRepository<UserDonation, Long> {

    boolean existsByUserIdAndDonationIdAndRegistration(Long userId, Long donationId, String registration);
}
