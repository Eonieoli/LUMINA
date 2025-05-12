package com.lumina.backend.donation.service;

import com.lumina.backend.donation.model.request.DoDonationRequest;
import com.lumina.backend.donation.model.response.GetDetailDonationResponse;
import com.lumina.backend.donation.model.response.GetDonationResponse;

import java.util.List;
import java.util.Map;

public interface DonationService {

    List<GetDonationResponse> getDonation(Long userId);

    void doDonation(Long userId, DoDonationRequest request);

    Boolean toggleDonationSubscribe(Long userId, Long donationId);

    Map<String, Object> getSubscribeDonation(Long userId);

    Map<String, Object> searchDonation(String keyword, int pageNum);

    GetDetailDonationResponse getDetailDonation(Long userId, Long donationId);
}
