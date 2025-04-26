package com.lumina.backend.donation.repository;

import com.lumina.backend.donation.model.entity.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    Page<Donation> findByStatusTrue(Pageable pageable);

    Optional<Donation> findByDonationName(String donationName);
}
