package com.lumina.backend.donation.service;

import java.util.Map;

public interface DonationService {

    Map<String, Object> getDonation(Long userId, int pageNum);
}
