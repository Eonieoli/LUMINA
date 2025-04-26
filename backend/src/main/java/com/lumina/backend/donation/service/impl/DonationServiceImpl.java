package com.lumina.backend.donation.service.impl;

import com.lumina.backend.donation.model.entity.Donation;
import com.lumina.backend.donation.model.response.GetDonationResponse;
import com.lumina.backend.donation.repository.DonationRepository;
import com.lumina.backend.donation.repository.UserDonationRepository;
import com.lumina.backend.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final UserDonationRepository userDonationRepository;


    @Override
    public Map<String, Object> getDonation(
            Long userId, int pageNum) {

        PageRequest pageRequest = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Donation> donationPage = donationRepository.findByStatusTrue(pageRequest);

        List<GetDonationResponse> donationList = donationPage.getContent().stream()
                .map(donation -> {
                    Boolean isSubscribe = userDonationRepository.existsByUserIdAndDonationIdAndRegistration(
                            userId, donation.getId(), "USER");
                    return new GetDonationResponse(
                            donation.getId(), donation.getDonationName(), isSubscribe
                    );
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", donationPage.getTotalPages());
        result.put("currentPage", pageNum);
        result.put("donations", donationList);

        return result;
    }
}
