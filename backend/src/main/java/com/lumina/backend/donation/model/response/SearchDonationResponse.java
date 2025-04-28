package com.lumina.backend.donation.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchDonationResponse {

    private Long donationId;
    private String donationName;
}
