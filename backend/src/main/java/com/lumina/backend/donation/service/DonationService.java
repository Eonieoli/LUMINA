package com.lumina.backend.donation.service;

import java.util.Map;

public interface DonationService {

    Map<String, Object> getDonation(Long userId, int pageNum);

    /**
     * 기부처에 대한 구독 토글 메서드
     */
    Boolean toggleDonationSubscribe(Long userId, Long donationId);

    Map<String, Object> getSubscribeDonation(Long userId, int pageNum);

    Map<String, Object> searchDonation(String keyword, int pageNum);
}
