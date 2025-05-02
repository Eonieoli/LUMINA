package com.lumina.backend.donation.repository;

import com.lumina.backend.donation.model.entity.Donation;
import com.lumina.backend.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByStatusTrue();

    Optional<Donation> findByDonationName(String donationName);

    Page<Donation> findByDonationNameContaining(String keyword, Pageable pageable);

    List<Donation> findByCategoryId(Long categoryId);
}
