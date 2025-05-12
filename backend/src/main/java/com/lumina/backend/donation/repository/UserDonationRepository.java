package com.lumina.backend.donation.repository;

import com.lumina.backend.donation.model.entity.UserDonation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDonationRepository extends JpaRepository<UserDonation, Long> {

    Optional<UserDonation> findByUserIdAndDonationIdAndRegistration(Long userId, Long donationId, String registration);


    boolean existsByUserIdAndDonationIdAndRegistration(Long userId, Long donationId, String registration);

    void deleteByUserIdAndRegistration(Long userId, String registration);


    @EntityGraph(attributePaths = {"donation"})
    List<UserDonation> findByUserIdAndRegistration(Long userId, String registration);
}
